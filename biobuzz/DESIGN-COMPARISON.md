# BIOBUZZ 2026-27 — Design 1 vs Design 2, and the design I'd actually build

Companion to `DESIGN-REVIEW.md` (full rules analysis) and `match_model.py` (the numbers).
Read the review first if you have not — this document assumes its rules citations.

The three designs on the table:

| | Name | One-line description |
|---|---|---|
| **1** | **Sorter** | Floating roller intake, *tuned wheel spacing*: narrow gaps at the edges for POLLEN, wide gaps in the middle sized so NECTAR is grabbed and POLLEN falls through. Ramp + second shaft transport. Hooded flywheel. 3-section walled hopper. Colour/distance sensors, camera. |
| **2** | **Even-spacing** | Identical layout, but the wheel spacing is uniform → works for POLLEN only. Same launcher, same sensors. |
| **3** | **Slide + claw** | The same launcher layout plus a linear slide and a claw to handle NECTAR (the flower tool). |
| **D** | **Magazine** (new, recommended) | Even spacing *plus* a properly compliant intake, a **single-file 4-round magazine** instead of a hopper, a fixed closed-loop hooded flywheel, and a software playbook built around a 10-second trip. |

---

## 1. Design 1 vs Design 2 — the answer in one table

| | **1 — Sorter** | **2 — Even spacing** |
|---|---|---|
| Elements it can swallow | POLLEN reliably; NECTAR through the wide centre gaps | POLLEN reliably; NECTAR **only if the intake is compliant enough** |
| Motorised shafts | 2–3 (intake, transfer, flywheel) | 1–2 (intake, flywheel) |
| Extra parts vs the other | spaced wheel set, extra wheels, hopper walls ×2, an exit path for the rejected POLLEN, more structure | none |
| Jam surfaces | **many** — the wide gaps are traps (see §2) | few |
| Foul risk | unchanged | unchanged |
| **Extra TIPs from the sorting itself** | **0** | **0** |
| Cycles until a TIP | 2 trips if a full NECTAR load lands, 3 if POLLEN | 3 trips with POLLEN at 85% accuracy; 2 with NECTAR |
| Build/validate time for a small team | ~2–3 extra weeks and the highest-risk mechanism on the robot | baseline |
| What it does that 2 cannot | separates a *mixed* pile into pollen and nectar **on the way in** | nothing — but see §3 |

**Verdict: build Design 2, not Design 1 — and then upgrade Design 2 rather than reverting to Design 1.**

The reason is not "simpler is nicer". It is that **the sorter buys you nothing you can spend.**
Points in BIOBUZZ come from HIVE TIPs (20), elements left in the CELL (2), GARDEN elements (1),
owned-FLOWER elements (2) and PARK/LEAVE (13 per robot). POLLEN and NECTAR are
**interchangeable for every one of those except the FLOWER**, and to score a FLOWER you must
*lift and drop* an element through a 4.00 in hole 21.5 in up (`9.7`, `G418`) — a flywheel
cannot do it, sorted or not. So:

> Sorting POLLEN away from NECTAR at the intake optimises the one resource decision that
> the game never asks you to make.

And you already make the decision that *does* matter — **which resource to collect** — for free,
by choosing where to drive. Your own NECTAR is either in your CELL (spilled on the floor after
the first TIP) or handed to you in the LOADING ZONE by the human player one per TIP (`G426`).
POLLEN is in the GARDENs, the FLOWERs and on the floor. Those two piles are in different places;
"drive to the right pile" is a driver decision, not a mechanism.

---

## 2. Why the tuned spacing is worse than neutral, not just unnecessary

Three independent mechanisms, all working against you:

**(a) The wide gap is a trap, not a filter.** Two coaxial wheels with a clear gap `G = S − w`
and a ball of diameter `d`:

* `G < d` → the ball is pinched between the wheel faces and driven. Good.
* `G > d` → the ball goes into the gap and **lands on the shaft**, below the wheel rims, where
  nothing is driving it. It does not "roll out". Best case the spinning wheel faces flick it back
  out; the normal case is a ball parked on the shaft, plugging the slot you opened for NECTAR.
  Getting it out requires a *second, lower* collection path — a whole extra mechanism.

**(b) The tolerance window is small and the elements are soft.** Sorting window ≈
`2.9 in < G < 3.5 in` (0.6 in). `9.8` says outright that POLLEN and NECTAR "are not perfectly
spherical and may vary in size. Teams should plan for this variation." Polyethylene balls also
deform a little under pinch load, which eats further into the margin.

**(c) Every gap narrower than a ball is a place a ball can wedge — and you now have both kinds.**
`G407` caps you at 4 elements, so the robot's *capacity* is tiny; a sorter's only job is to make
the intake reject things, and rejection in a 15 in wide mechanism means a ball has to be somewhere
it can block. The most common match-losing failure in FTC intakes is not "wrong element" — it is
"one ball, wrong place, 30 seconds lost".

**Where Design 1 is genuinely right:** you correctly identified that the size difference
*can* be exploited, and you correctly saw that NECTAR is the valuable element. The exploitable
version of that insight is not mechanical — it is **"NECTAR is 1.65× the mass per element, and
carrying NECTAR is what makes two trips per TIP possible."**

---

## 3. The real fix for "Design 2 only works for POLLEN"

If a uniform-spacing intake refuses NECTAR, the problem is almost never the *spacing*. It is the
**entry aperture and the compliance**:

* A fixed aperture sized around a 2.8 in ball will physically block a 3.6 in ball (the roller's
  bottom edge, the ramp lip, the frame's front rail — any of them).
* A hard-mounted roller that is right for POLLEN can be too tight for NECTAR: the ball gets pushed
  forward and rolls away instead of being drawn into the pinch.

So Design 2's upgrade is mechanical, small, and it is the one the official 2026-27 goBILDA
StarterBot uses (a GripForce Gecko-wheel intake that pulls POLLEN straight out of a FLOWER):

1. **Make the aperture ≥ 4.0 in nominal** wherever a ball has to *enter*, and let a
   **spring-loaded roller close down onto the ball** to grip it. Entry and grip are different
   requirements; do not solve them with one fixed dimension.
2. **Float travel ≥ 1.0 in** (the difference between a 2.8 in and a 3.6 in ball is 0.8 in — your
   current float must cover that plus field variation).
3. **Soft, compliant wheel surfaces** — they deform around the bigger ball instead of stalling.
   Gear the intake for torque, not speed (the intake does not need RPM; the flywheel does).
4. **Test it as an acceptance criterion:** 20 POLLEN + 10 NECTAR, 100% capture, zero manual
   intervention, three runs in a row. If a NECTAR bounces out, the aperture or the spring is wrong.

Do that and the "POLLEN only" limitation disappears, with no sorting and no extra shaft.

---

## 4. Quantified comparison (from `match_model.py`)

Mechanical costs are *illustrative and printed in the script* so you can argue with them:
Design 1 loses 20% of POLLEN into the wide gaps (it has to be re-acquired, so the same load takes
longer) and jams once per 8 trips for 6 s. Design 2 is the baseline 12 s trip. Design D gets to
10 s by shooting from one fixed spot out of a single-file magazine, and 90% accuracy from
closed-loop flywheel speed.

```
1) LOADOUT COMPARISON -- one robot, 12 s trips, hit rate 85%, recapture 85%
   (throughput ceiling: assumes spilled elements are re-collected)
==============================================================================
scenario                       trip/tip  tips  AUTO  TELEOP  MATCH           RP
-------------------------------------------------------------------------------
A  4 POLLEN / trip                    3   5.3    28      96    124        POLL1
B  3 NECTAR + 1 POLLEN                2   6.4     8     137    145        POLL1
C  4 NECTAR / trip                    2   7.1     8     147    155  POLL1+POLL2
D  2 NECTAR + 2 POLLEN                2   5.7     8     128    136        POLL1

3b) THE MECHANICAL COST OF THE SORTER -- Design A vs B vs D
    A = tuned-spacing sorter   B = even spacing (pollen-capable)
    D = magazine design recommended in DESIGN-COMPARISON.md
    (illustrative mechanical costs, printed so you can argue with them)
==============================================================================
scenario                       trip/tip  tips  AUTO  TELEOP  MATCH           RP
-------------------------------------------------------------------------------
1  sorter (15.8 s trips)              3   4.3    28      75    103        POLL1
2  even spacing (12.0 s)              3   5.3    28      96    124        POLL1
D  magazine (10.0 s)                  3   6.4    28     121    149        POLL1
    effective trip time:  A 15.8 s   B 12.0 s   D 10.0 s
    A's mechanical cost: 20% of POLLEN lost into the wide gaps
    (has to be re-acquired) + 1 jam per 8 trips, 6 s to clear.
    A's upside: element-type selection -- worth 0 extra TIPs, because
    POLLEN is never harmful and a flywheel cannot place NECTAR into a
    FLOWER (G418 / 9.7) anyway.

3c) WHAT ONE SECOND OF TRIP TIME IS WORTH -- 4 POLLEN per trip
==============================================================================
 cycle_s  trips   tips  TIP pts  pts lost
      10   12.0    6.1      123         -
      11   10.9    5.7      113         9
      12   10.0    5.3      105         8
      13    9.2    4.9       99         7
      14    8.6    4.7       93         6
      15    8.0    4.4       88         5
    ('pts lost' = MATCH points given away by adding one second)
    => roughly 7 points per second at a 12 s cycle; treat trip time as the
       currency you are spending on every mechanism you add.

3d) TRIPS PER TIP vs HOW MANY ELEMENTS ACTUALLY LAND
==============================================================================
  elements landing    4 POLLEN    4 NECTAR
               4.0           2           2
               3.5           3           2
               3.0           3           2
               2.5           4           2
               2.0           4           3
    NECTAR stays at two trips per TIP even when one of four misses;
    POLLEN needs a perfect 4/4 -- that is the real value of NECTAR.
```

Three things to read out of this:

1. **Design 1 vs 2 is worth about 1 TIP (≈21 points) even before you consider the jam risk** —
   and Design 1's sorter is *never* the thing that earns those points back.
2. **A second of trip time is worth ~7 points.** That is the exchange rate for every mechanism
   you bolt on. The sorter, the walls and the second shaft each have to buy back their own seconds.
3. **NECTAR is a robustness play, not just a mass play.** 4 NECTAR per trip still gives 2 trips
   per TIP when only 3 of 4 land; 4 POLLEN needs a perfect 4/4 to stay at 2 trips. That is why
   *choosing* NECTAR (by driving to it) beats *sorting* it.

---

## 5. Design D — the design I'd build instead

**Concept:** the robot is a *recycling shuttle* around the HIVE. It carries exactly four elements
in a **single-file magazine**, shoots them from **one calibrated spot**, and goes straight back to
the pile that a TIP just dumped on the floor. Everything else on the robot exists to make that
loop 10 seconds long.

### 5.1 Mechanical

| Assembly | Spec | Why |
|---|---|---|
| Drivetrain | 15 × 15 in mecanum, 4 × 5203-class (or 5202 geared up) on 96–104 mm GripForce mecanum, battery low, **belly pan + side skirts** | no launch zone this year → holonomic aiming. Skirts stop loose balls becoming a chassis trap |
| Intake | Full-width floating roller, gecko/compliant wheels, **uniform gaps ≤ 2.5 in**, roller bottom **0.3–0.7 in** above the tile, **≥1.0 in spring travel**, aperture ≥ 4.0 in at entry, **quick-release** (2 bolts + 1 connector) | swallows 2.8 in and 3.6 in with one geometry (see §3). Replaceable nose = the failure you can fix in a pit |
| Transfer | **Passive polycarbonate chute** from the roller mouth into the magazine entry. Add one driven "helper" roller only if you measure a stall | the second shaft is one more motor and two more jam points than the task needs |
| Magazine | Single-file channel holding **exactly 4**, gravity-fed, ≥ 4.2 in clear width, no ledges; **break-beam at entry** (counts 1–4 and stops the intake), **servo gate at exit** (releases 1 ball) | mechanically enforces `G407`; gives you "shoot 4 in quick succession" without a hopper full of pressure; makes counting trivial |
| Launcher | **One** 5202-class motor, 3–4 in urethane flywheel, fixed curved / spring-loaded hood, angle ~55°, exit ~22 in high, **closed-loop velocity** (`setVelocity` + encoder) | a hood is the only launcher geometry that tolerates both ball sizes from one RPM. Fixed because repeatable beats adjustable |
| Shed | Second gate (or the intake in reverse) dumping the magazine out the bottom | `G407` is a MAJOR FOUL + YELLOW CARD if STRATEGIC. You want a reflex, not a debate |
| Lights | Small RGB strip / LED: **SHOT READY**, and loaded composition (yellow / red / blue) | free points: the driver stops guessing |

### 5.2 Electrical budget (fits `R503` with room to spare)

| Port | Device |
|---|---|
| 0–3 | 4 × drive motors |
| 4 | Intake |
| 5 | Flywheel |
| 6 | **reserved** — transfer helper, or the flower lift in Phase 3 |
| 7 | **reserved** — the fix you have not thought of yet |

Servos (8): release gate (1), shed (1), spare (1). Sensors: colour at the intake mouth (classify
each ball as it enters, `G408` reject), 2 × break-beam (magazine count, gate staged), optional ToF.
Plan your I2C ports on paper first — the Control Hub has four and they do not multiply.

### 5.3 Software — where the extra TIPs actually come from

| Routine | Behaviour |
|---|---|
| `AUTO_TIP` | Drive to the marked spot, spin up, fire **3 of the 4 pre-loaded POLLEN**, then PARK. The CELL already holds 3 NECTAR, so those three balls are worth **20 points** (`12.3` calibration), on top of LEAVE 3 + PARK 5 = **28 points** |
| `SHOOT_ALL` | Fire magazine contents at a fixed cadence; abort on a current spike or a missing ball at the gate |
| `COUNT_4` | Hard stop at 4 (`G407`), with `SHED` as the escape |
| `NECTAR_REJECT` | Colour sensor sees **opponent** NECTAR → reverse the intake for ~0.4 s (`G408`) |
| `SETTLE` | After any HIVE TIP, block launching for ~1.5 s | §10.5.1 warns that launching at the down-facing CELL during a tip can stop the tip being scored |
| `TRIP_TIMER` | Telemetry stopwatch per cycle, logged to a file in practice | the only mechanism upgrade that reliably pays for itself is a shorter trip |

### 5.4 Strategy (this is the part that scores)

1. **AUTO:** 3 pollen → TIP → PARK. 28 points, no vision required, works on a bad day.
2. **First 90 s of TELEOP:** the recycling loop. TIP → the CELL dumps your elements on the floor
   next to the HIVE → 4 of them back into the magazine → TIP again. Prefer NECTAR loads where you
   can get them (1.65× the mass, and it survives a miss). Collect the human player's NECTAR as
   soon as each TIP unlocks it (`G426`).
3. **Last 60 s:** if you have a flower module, place NECTAR **last** (the top-most NECTAR owns
   every element in that FLOWER — it is winner-take-all) and remember NECTAR cannot legally enter
   a FLOWER before this window (`G410`). If you do not, keep tipping and push leftovers into the
   GARDEN on your way past (1 point each, no mechanism).
4. **Final seconds:** PARK (5). Two robots parking and leaving together is the SWARM RP (≥16).

### 5.5 Phase plan with hard decision gates

| Phase | Deliverable | Gate to pass before moving on |
|---|---|---|
| **P1** | Drivetrain + intake + magazine + shed + counting. No launcher. | 4 elements captured in ≤ 4 s, from floor and from your GARDEN, 5 runs in a row, no jams, count always correct |
| **P2** | Flywheel + hood + closed-loop velocity + `AUTO_TIP` + `SETTLE` | ≥ 80% of shots into the CELL from the marked spot, 20 shots, both ball sizes |
| **P3** (only if P1+P2 are boring) | Single-motor flower lift/drop on the reserved motor | Places a POLLEN *and* a NECTAR through the top ring, 5/5, with a software interlock at the 60 s mark |

---

## 6. Improvements, ranked by points-per-hour-of-work

Do these in order. Everything here works for **both** Designs 1 and 2 unless noted.

| # | Change | Gain | Effort | Risk |
|---|---|---|---|---|
| 1 | **Delete the tuned spacing** — one uniform gap ≤ 2.5 in | removes the top jam source, removes parts | 1 evening in CAD | none |
| 2 | **Fix the intake for both ball sizes** the §3 way (aperture ≥ 4.0 in + ≥1.0 in float + soft wheels) | NECTAR becomes usable → 2 trips per TIP instead of 3 | 1 evening + a test rig | low |
| 3 | **Delete the 3-section hopper** → single-file 4-round magazine | `G407`-proof, faster reload, fewer walls | 1 weekend | low |
| 4 | **Shoot the pre-loads in AUTO** (`AUTO_TIP`) | +20 points for zero hardware | 1 evening of code | low |
| 5 | **Closed-loop flywheel velocity + one marked shooting spot** | every miss is 25–41 g of a 199 g TIP; this is the cheapest accuracy you will ever buy | 1 evening + a chart | low |
| 6 | **`SETTLE` after a TIP** | protects TIPs your alliance already earned | 20 minutes of code | none |
| 7 | **`COUNT_4` + `SHED` + break-beam** | avoids MAJOR FOUL / YELLOW CARD (`G407`) | 1 evening | none |
| 8 | **Colour reject at the mouth** (`G408`) | avoids a YELLOW CARD, protects NECTAR loads | 1 evening | low |
| 9 | **Belly pan + side skirts + replaceable intake nose** | a ball under the chassis or a torn-off intake is a lost match | 1 weekend | none |
| 10 | **`TRIP_TIMER` logging in practice** | turns "feels faster" into a number you can defend | 30 minutes | none |
| 11 | **NECTAR-first driving discipline** (coach the driver, not the robot) | the single largest strategic gain available to you | free | none |
| 12 | **Flower lift/drop on the spare motor** — Phase 3 only | 2 points per element in an owned FLOWER + 5 bottom-NECTAR | 2–3 weekends | **high** |
| 13 | **Do not build:** turret, adjustable hood, claw, linear slide | saves 1–2 motors of 8 and the whole of your October | free | none |

### The three-way comparison, condensed

* **Design 1** optimises the wrong variable (sorting) and pays for it in seconds and jams.
* **Design 2** is the right baseline, but its "POLLEN only" limit is a *compliance* bug, not a
  spacing feature — fix the intake and Design 2 becomes Design 1's best case without Design 1's cost.
* **Design 3** (slide + claw) is the only one that can score FLOWERs, and that is the only reason
  to accept it. But `G415` forbids grabbing ARENA elements, you never need to grip a ball, and the
  slide eats 1–2 of your 8 motors. If you want FLOWERs, build **a single-motor lift with a passive
  cup** — and build it in October, once the HIVE loop is boring.

> **If you take one thing from this document:** your robot's score is set by how many times it can
> complete a 10-second trip around the HIVE, and NECTAR is what makes that loop short. Spend your
> design budget on the intake, the magazine and the shot — not on sorting.
