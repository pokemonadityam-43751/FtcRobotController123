#!/usr/bin/env python3
"""
BIOBUZZ 2026-27 -- HIVE-tip throughput and MATCH-point estimator.

Hard-coded constants come from the official 2026-27 sources:

  * BIOBUZZ Competition Manual V1
      9.6.2  HIVE / CELL geometry        (opening ~20 x 14 in, lip 53.5 in up, CELL 12 in deep)
      9.7    FLOWER geometry             (top opening 4.0 in dia, 21.5 in above tiles)
      9.8    SCORING ELEMENTS            (POLLEN 2.8 in / 0.055 lb, NECTAR 3.6 in / 0.091 lb)
      10.3.1 staging                     ([3] NECTAR in the upward-facing CELL at start)
      10.5.5 Table 10-2 / Table 10-3     (point values, RP thresholds)
      G407   no more than 4 SCORING ELEMENTS controlled at a time
      G410   NECTAR only into FLOWERS in the last 60 s
      G426   human player enters 1 NECTAR per HIVE TIP (all remaining after 60 s)
      R105   18 x 24 x 29 in expansion envelope
      R503   8 motors / 8 servos
  * BIOBUZZ Event Field Setup Guide V1.0 section 12.3
      HIVE is calibrated to TIP at [8] POLLEN  or  [3] NECTAR + [3] POLLEN  (~199 g)

KEY MODEL INSIGHT (and the point of this script):
The 4-element control limit means the heaviest possible single load is
4 x NECTAR = 165 g, which is 83% of one TIP.  So *no* single trip can tip a
HIVE after the first one (the first one only needs 3 POLLEN because 3 NECTAR
are already sitting in the CELL).  Tips therefore arrive in pairs of trips, and
the whole game reduces to: how short can you make a trip?

Because a TIP dumps the CELL contents back onto the tile floor, the same ~8
elements can be recycled indefinitely, so tip throughput is limited by trip
time, not by the 40 POLLEN on the FIELD.  Recapture losses are modelled with
--recapture.

Deterministic expected-value model (fractional elements), not a rules engine.

    python3 match_model.py
    python3 match_model.py --help
"""

from __future__ import annotations

import argparse
import math
from dataclasses import dataclass

# --------------------------------------------------------------------------
# Game constants
# --------------------------------------------------------------------------
POLLEN_MASS_G = 25.0          # 0.055 lb
NECTAR_MASS_G = 41.3          # 0.091 lb
TIP_MASS_G = 199.0            # [8] POLLEN = 200 g | [3] N + [3] P = 199 g
START_CELL_NECTAR = 3         # already in the upward-facing CELL at t = 0
CONTROL_LIMIT = 4             # G407
CARGO_LIMIT = CONTROL_LIMIT   # elements you may carry/shoot per trip
NECTAR_IN_TRAY = 5            # per ALLIANCE, fed in by the human player
AUTO_S = 30
TELEOP_S = 120
FLOWER_WINDOW_S = 60          # G410: NECTAR into FLOWERs only in the last 60 s

P_TIP = 20                    # MATCH points (AUTO or TELEOP)
P_CELL_LEFTOVER = 2           # per element still in the CELL at end of MATCH
P_GARDEN = 1                  # per element in the GARDEN
P_FLOWER_OWNED = 2            # per element in an owned FLOWER
P_BOTTOM_NECTAR = 5
P_LEAVE = 3                   # AUTO
P_PARK = 5                    # AUTO and TELEOP

RP_SWARM_LEAVE_PARK = 16      # combined LEAVE + PARK points, ALLIANCE total
RP_TIPS_POLLINATOR_1 = 4
RP_TIPS_POLLINATOR_2 = 7


@dataclass
class Result:
    label: str
    trips: float = 0.0
    trips_per_tip: float = 0.0
    tips: float = 0.0
    auto_points: float = 0.0
    teleop_points: float = 0.0
    leftover_elements: float = 0.0
    loss_per_tip_g: float = 0.0

    @property
    def match_points(self) -> float:
        return self.auto_points + self.teleop_points

    @property
    def rps(self) -> str:
        rp = []
        if self.tips >= RP_TIPS_POLLINATOR_1:
            rp.append("POLL1")
        if self.tips >= RP_TIPS_POLLINATOR_2:
            rp.append("POLL2")
        return "+".join(rp) if rp else "-"


def estimate(
    label: str,
    cycle_s: float,
    hit_rate: float,
    pollen_per_load: float,
    nectar_per_load: float,
    recapture: float = 0.85,
    garden_elements: float = 0.0,
    flower_elements: float = 0.0,
    bottom_nectar: bool = False,
    park: bool = True,
    leave: bool = True,
) -> Result:
    """Result for ONE robot; RP thresholds are ALLIANCE level (2 robots share a HIVE)."""
    r = Result(label=label)
    load = pollen_per_load + nectar_per_load
    if load <= 0 or load > CONTROL_LIMIT:
        raise ValueError("0 < elements per load <= 4 (G407)")

    delivered_pollen = pollen_per_load * hit_rate
    delivered_nectar = nectar_per_load * hit_rate
    mass_per_trip = (
        delivered_pollen * POLLEN_MASS_G + delivered_nectar * NECTAR_MASS_G
    )
    try:
        r.trips_per_tip = math.ceil(TIP_MASS_G / mass_per_trip)
    except ZeroDivisionError:
        r.trips_per_tip = math.inf
    # Elements that land in the CELL are all returned to the floor when the
    # HIVE tips, so they can be recycled.  (1 - recapture) of that mass is lost
    # per tip and has to be replaced from GARDEN / FLOWER / floor stock.
    loss_per_tip_g = TIP_MASS_G * (1.0 - recapture)

    # ---- AUTO ------------------------------------------------------------
    if leave:
        r.auto_points += P_LEAVE
    if park:
        r.auto_points += P_PARK
        r.teleop_points += P_PARK
    # One AUTO trip shooting the 4 pre-loads into a CELL that already holds
    # 3 NECTAR (124 g).  3 landing POLLEN (75 g) completes the tip.
    auto_trips = 1 if AUTO_S >= cycle_s else 0
    if auto_trips and delivered_pollen >= 3.0:
        r.tips += 1.0
        r.auto_points += P_TIP

    # ---- TELEOP ----------------------------------------------------------
    trips = TELEOP_S / cycle_s          # expected trips (fractional)
    r.trips = trips
    total_mass = trips * mass_per_trip
    teleop_tips = total_mass / TIP_MASS_G
    r.tips += teleop_tips
    r.teleop_points += teleop_tips * P_TIP
    # Whatever is in the CELL when the clock runs out still scores 2 points each.
    mass_per_element = mass_per_trip / load
    r.leftover_elements = (total_mass % TIP_MASS_G) / mass_per_element
    r.teleop_points += r.leftover_elements * P_CELL_LEFTOVER
    r.loss_per_tip_g = loss_per_tip_g

    # ---- Optional extras -------------------------------------------------
    r.teleop_points += garden_elements * P_GARDEN
    if flower_elements:
        r.teleop_points += flower_elements * P_FLOWER_OWNED
        if bottom_nectar:
            r.teleop_points += P_BOTTOM_NECTAR
    return r


def effective_cycle(
    base_s: float,
    pollen_loss: float = 0.0,
    jam_per_trip: float = 0.0,
    jam_clear_s: float = 0.0,
) -> float:
    """Trip time after paying for mechanical complexity.

    pollen_loss : fraction of a pickup swallowed by the mechanism that then has
                  to be re-acquired (spreads the same load over more time)
    jam_per_trip * jam_clear_s : expected seconds of un-jamming per trip
    """
    if not 0.0 <= pollen_loss < 1.0:
        raise ValueError("pollen_loss must be in [0, 1)")
    return base_s / (1.0 - pollen_loss) + jam_per_trip * jam_clear_s


def table(rows: list[Result]) -> str:
    head = (
        f"{'scenario':<30}{'trip/tip':>9}{'tips':>6}"
        f"{'AUTO':>6}{'TELEOP':>8}{'MATCH':>7}{'RP':>13}"
    )
    out = [head, "-" * len(head)]
    for r in rows:
        tpt = "-" if math.isinf(r.trips_per_tip) else f"{r.trips_per_tip:.0f}"
        out.append(
            f"{r.label:<30}{tpt:>9}{r.tips:>6.1f}"
            f"{r.auto_points:>6.0f}{r.teleop_points:>8.0f}"
            f"{r.match_points:>7.0f}{r.rps:>13}"
        )
    return "\n".join(out)


def main() -> None:
    ap = argparse.ArgumentParser(description="BIOBUZZ tip / point estimator")
    ap.add_argument("--hit", type=float, default=0.85,
                    help="fraction of launched elements that stay in the CELL")
    ap.add_argument("--recapture", type=float, default=0.85,
                    help="fraction of spilled elements the robot re-collects")
    args = ap.parse_args()

    print("=" * 78)
    print("1) LOADOUT COMPARISON -- one robot, 12 s trips, hit rate "
          f"{args.hit:.0%}, recapture {args.recapture:.0%}")
    print("   (throughput ceiling: assumes spilled elements are re-collected)")
    print("=" * 78)
    rows = [
        estimate("A  4 POLLEN / trip", 12, args.hit, 4, 0, args.recapture),
        estimate("B  3 NECTAR + 1 POLLEN", 12, args.hit, 1, 3, args.recapture),
        estimate("C  4 NECTAR / trip", 12, args.hit, 0, 4, args.recapture),
        estimate("D  2 NECTAR + 2 POLLEN", 12, args.hit, 2, 2, args.recapture),
    ]
    print(table(rows))

    print()
    print("=" * 74)
    print("2) TRIP-TIME SENSITIVITY -- 4 POLLEN per trip, ONE robot")
    print("=" * 74)
    print(f"{'cycle_s':>8}{'trips':>7}{'trip/tip':>10}{'tips':>6}"
          f"{'TELEOP':>8}{'MATCH':>7}{'RP':>13}")
    for c in (8, 10, 12, 15, 18, 22, 30):
        r = estimate(f"{c} s", c, args.hit, 4, 0, args.recapture)
        print(f"{c:>8}{r.trips:>7.0f}{r.trips_per_tip:>10.0f}{r.tips:>6.1f}"
              f"{r.teleop_points:>8.0f}{r.match_points:>7.0f}{r.rps:>13}")

    print()
    print("=" * 78)
    print("2b) SHOT-ACCURACY SENSITIVITY -- 12 s trips, 4 POLLEN per trip")
    print("=" * 78)
    print(f"{'hit rate':>10}{'g/trip':>9}{'tips':>7}{'TELEOP':>8}{'MATCH':>7}{'RP':>13}")
    for h in (0.60, 0.70, 0.80, 0.90, 1.00):
        r = estimate("x", 12, h, 4, 0, args.recapture)
        print(f"{h:>10.0%}{4*h*POLLEN_MASS_G:>9.0f}{r.tips:>7.1f}"
              f"{r.teleop_points:>8.0f}{r.match_points:>7.0f}{r.rps:>13}")

    print()
    print("=" * 74)
    print("3) ALLIANCE TOTAL -- two robots sharing one HIVE (tips add up)")
    print("=" * 74)
    print(f"{'cycle_s':>8}{'tips/robot':>12}{'tips ALLIANCE':>15}{'POLL1':>8}{'POLL2':>8}")
    for c in (10, 12, 15, 18, 22, 30):
        r = estimate("x", c, args.hit, 4, 0, args.recapture)
        alliance = r.tips + max(0.0, r.tips - 1.0) * 0.85   # partner, slightly worse
        print(f"{c:>8}{r.tips:>12.1f}{alliance:>15.1f}"
              f"{'yes' if alliance >= 4 else 'no':>8}"
              f"{'yes' if alliance >= 7 else 'no':>8}")

    print()
    print("=" * 78)
    print("3b) THE MECHANICAL COST OF THE SORTER -- Design A vs B vs D")
    print("    A = tuned-spacing sorter   B = even spacing (pollen-capable)")
    print("    D = magazine design recommended in DESIGN-COMPARISON.md")
    print("    (illustrative mechanical costs, printed so you can argue with them)")
    print("=" * 78)
    a_cycle = effective_cycle(12.0, pollen_loss=0.20, jam_per_trip=1 / 8,
                              jam_clear_s=6.0)
    rows = [
        estimate(f"1  sorter ({a_cycle:.1f} s trips)", a_cycle, 0.85, 4, 0,
                 args.recapture),
        estimate("2  even spacing (12.0 s)", 12.0, 0.85, 4, 0, args.recapture),
        estimate("D  magazine (10.0 s)", 10.0, 0.90, 4, 0, args.recapture),
    ]
    print(table(rows))
    print(f"    effective trip time:  A {a_cycle:.1f} s   B 12.0 s   D 10.0 s")
    print("    A's mechanical cost: 20% of POLLEN lost into the wide gaps")
    print("    (has to be re-acquired) + 1 jam per 8 trips, 6 s to clear.")
    print("    A's upside: element-type selection -- worth 0 extra TIPs, because")
    print("    POLLEN is never harmful and a flywheel cannot place NECTAR into a")
    print("    FLOWER (G418 / 9.7) anyway.")

    print()
    print("=" * 78)
    print("3c) WHAT ONE SECOND OF TRIP TIME IS WORTH -- 4 POLLEN per trip")
    print("=" * 78)
    print(f"{'cycle_s':>8}{'trips':>7}{'tips':>7}{'TIP pts':>9}{'pts lost':>10}")
    prev = None
    for c in (10, 11, 12, 13, 14, 15):
        r = estimate("x", float(c), args.hit, 4, 0, args.recapture)
        tip_pts = r.tips * P_TIP
        delta = "-" if prev is None else f"{prev - tip_pts:.0f}"
        print(f"{c:>8}{r.trips:>7.1f}{r.tips:>7.1f}{tip_pts:>9.0f}{delta:>10}")
        prev = tip_pts
    print("    ('pts lost' = MATCH points given away by adding one second)")
    print(f"    => roughly {P_TIP * (4 * args.hit * POLLEN_MASS_G / TIP_MASS_G) * TELEOP_S / 12 / 12:.0f}"
          " points per second at a 12 s cycle; treat trip time as the")
    print("       currency you are spending on every mechanism you add.")

    print()
    print("=" * 78)
    print("3d) TRIPS PER TIP vs HOW MANY ELEMENTS ACTUALLY LAND")
    print("=" * 78)
    print(f"{'elements landing':>18}{'4 POLLEN':>12}{'4 NECTAR':>12}")
    for landed in (4.0, 3.5, 3.0, 2.5, 2.0):
        p = math.ceil(TIP_MASS_G / (landed * POLLEN_MASS_G))
        n = math.ceil(TIP_MASS_G / (landed * NECTAR_MASS_G))
        print(f"{landed:>18.1f}{p:>12}{n:>12}")
    print("    NECTAR stays at two trips per TIP even when one of four misses;")
    print("    POLLEN needs a perfect 4/4 -- that is the real value of NECTAR.")

    print()
    print("=" * 74)
    print("4) WHAT A NON-SHOOTING ROBOT IS WORTH (sanity floor)")
    print("=" * 74)
    floor = Result("LEAVE+PARK+AUTO PARK, no HIVE shots")
    floor.auto_points = P_LEAVE + P_PARK
    floor.teleop_points = P_PARK
    garden_only = Result("same + 4 elements in GARDEN")
    garden_only.auto_points = floor.auto_points
    garden_only.teleop_points = floor.teleop_points + 4 * P_GARDEN
    print(table([floor, garden_only]))

    print()
    print("-" * 74)
    print(f"TIP_MASS         {TIP_MASS_G:.0f} g  "
          f"= {TIP_MASS_G/POLLEN_MASS_G:.1f} POLLEN or 3 NECTAR + 3 POLLEN")
    print(f"CONTROL_LIMIT    {CONTROL_LIMIT} elements (G407) -> heaviest load "
          f"{CONTROL_LIMIT*NECTAR_MASS_G:.0f} g "
          f"= {CONTROL_LIMIT*NECTAR_MASS_G/TIP_MASS_G:.0%} of a TIP")
    print(f"TELEOP budget    {TELEOP_S} s : trips = {TELEOP_S}/cycle_s")
    print(f"NECTAR per trial {START_CELL_NECTAR} staged in the CELL + "
          f"{NECTAR_IN_TRAY} fed in by the human player (G426)")
    print(f"Supply note: at {args.recapture:.0%} recapture the ALLIANCE loses "
          f"~{TIP_MASS_G*(1-args.recapture):.0f} g of elements per TIP,")
    print(f"             i.e. ~{TIP_MASS_G*(1-args.recapture)/POLLEN_MASS_G:.1f} POLLEN-equivalents "
          f"per TIP to be replaced from GARDEN / FLOWER / floor stock.")
    print("Take-away: the free first TIP needs only 3 POLLEN (3 NECTAR are already")
    print("           in the CELL).  After that a 4-POLLEN load at 85% accuracy needs")
    print("           3 trips per TIP while a 3-NECTAR + 1-POLLEN load needs 2, so")
    print("           NECTAR + short trips are the two biggest levers you own.")


if __name__ == "__main__":
    main()
