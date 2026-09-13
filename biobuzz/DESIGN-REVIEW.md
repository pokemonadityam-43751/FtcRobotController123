# BIOBUZZ 2026-27 — Design Review

**Subject:** the "floating gecko-roller intake → polycarbonate ramp + second shaft → hooded
flywheel → colour/distance-sensored 3-section hopper, 15×15 mecanum" robot, versus
(a) the same layout with even wheel spacing (pollen only) and (b) a linear-slide + claw.

**All game facts below are cited to the official sources** — BIOBUZZ Competition Manual V1
(2026-27) sections `9.x`, `10.x`, `G4xx`, `R1xx/R5xx`, plus the BIOBUZZ Event Field Setup
Guide V1.0 §12. Section numbers are given so you can re-check anything before you cut metal.

---

## 0. Bottom line up front

| Question you asked | Answer |
|---|---|
| Can you use this design for the **HIVE**? | **Yes** — the architecture is right (it is essentially the same class of robot as the official goBILDA StarterBot: gecko-wheel intake + shooter). Four things must change, one of them is structural. |
| Can you use this design for the **FLOWERS**? | **Not with a flywheel.** Flower scoring requires *dropping* an element into a **4.00 in hole 21.5 in above the tile** (`9.7`), from the top only (`G418`), and NECTAR may only go in during the **last 60 s** (`G410`, MAJOR FOUL before that). That is a lift-and-place job. |
| Is the **tuned wheel spacing** a good idea? | **No — this is the part of the design I would drop first.** Two independent reasons: it does not do what you think mechanically (§3.3), and the rules already make it unnecessary (`G407` caps you at 4 elements; pollen is never harmful, only *opponent NECTAR* is, and that is a colour problem, not a size problem). |
| Is a **turret** a sensible "later" upgrade? | **No.** There is no LAUNCH ZONE this year — you may launch from anywhere — so a holonomic chassis *is* your turret. You also cannot afford the motor (`R503`: 8 motors total). |
| Is **linear slide + claw** the right flower tool? | **No.** A claw is the wrong end-effector: `G415` forbids grabbing ARENA elements, and you never need to grip a ball (rollers do that better and cheaper). The slide's *only* legitimate job here is flower placement, and it costs 1–2 of your 8 motors. |
| What is the single biggest lever on your score? | **Cycle time.** See §2.4 and `match_model.py`. |

### Grades

| Subsystem | Grade | One-line verdict |
|---|---|---|
| 15×15 mecanum drivetrain | **A−** | Correct choice for a game with no launch zone. Keep. |
| Floating single-shaft gecko intake | **A−** | Same class as the reference robot. Keep, with a "nose" change. |
| Tuned wheel spacing (pollen vs nectar sort) | **D** | Solves a problem the game does not have, and probably does not sort. Cut. |
| Ramp + second transfer shaft | **B** | Works, but it is one more motor and two more jam points than needed. |
| Hooded flywheel (fixed, adjustable later) | **B+** | Right launcher *type* for two ball sizes; needs real ballistics worked out. |
| Colour/distance sensors on intake | **A** | Promoted from "nice" to **required** by `G407`. |
| Camera for "precise launching" | **B+** | Useful for localisation/auto, not needed for accuracy — the target is 20×14 in. |
| 3-section walled hopper | **D** | Illegal to fill, and the walls buy you nothing. Cut. |
| Overall concept for a small team | **B** | Strong instincts, ~25% too much robot. Simpler version below scores the same or better. |

---

## 1. Sources this review is based on

| Source | What it gave |
|---|---|
| BIOBUZZ Competition Manual V1 §9.6.1/9.6.2 | Frame 49.46 × 38.95 in, pivots **43.95 in** above tiles; CELL opening **~20 in wide × 14 in tall × 12 in deep**; 30° tilt; bottom of the opening **53.5 in** above tiles, top **65.6 in** |
| §9.7 | FLOWER top opening **4.0 in dia, 21.5 in above tiles**; 1.25 in backstop; bottom Retrieval Opening **3.55 in tall × 3.57 in deep**; lower ring 0.43 in tall with a **2.79 in** hole |
| §9.8 | POLLEN 2.8 in / 0.055 lb; NECTAR 3.6 in / 0.091 lb; 40 / 8 / 8 on the field; "not perfectly spherical… plan for this variation" |
| §9.9 | AprilTag clusters (4 tags, 36h11, 3.25 in) on the **bottom face of each CELL**, facing down at the tiles |
| §10.3.1 | Staging: 4 POLLEN per ROBOT pre-loaded, 4 per GARDEN, 4 per FLOWER; **3 NECTAR already in the upward-facing CELL**; 5 NECTAR per ALLIANCE AREA |
| §10.5 / Table 10-2 | TIP 20, element remaining in CELL 2, owned-FLOWER element 2, Bottom-NECTAR bonus 5, GARDEN element 1, PARK 5 (AUTO+TELEOP), LEAVE 3 |
| Table 10-3 | SWARM RP ≥16 combined LEAVE+PARK; **POLLINATOR 1 RP = 4 TIPS; POLLINATOR 2 RP = 7 TIPS** |
| `G407` | **No more than 4 SCORING ELEMENTS controlled at a time** |
| `G408` / `G409` | No controlling opponent NECTAR / no catching elements released by a tipping HIVE |
| `G410` / `G417` / `G418` | NECTAR into FLOWERs only in the last 60 s; HIVE only via LAUNCHING into the upward-facing CELL; FLOWERs fed from the top, POLLEN only removed from the bottom |
| `G415` / `G406` | No grabbing/attaching to ARENA elements; no ARENA damage or mess |
| `R102` / `R105` / `R503` | 18 in starting cube; **18 × 24 × 29 in** expansion envelope; **8 motors / 8 servos** |
| Event Field Setup Guide §12.3 | HIVE calibration: tips with **[8] POLLEN** *or* **[3] NECTAR + [3] POLLEN** — i.e. **≈199 g** of element mass |

---

## 2. The game facts that actually drive robot design

### 2.1 NECTAR is 1.65× the element for 1.3× the ball
POLLEN 25.0 g, NECTAR 41.3 g. With a hard 4-element cap, **mass per trip** is what buys
hive tips, so a 4-NECTAR load is worth 165 g versus 100 g for 4 POLLEN.

### 2.2 The HIVE tips on ~199 g of element mass
Calibration target is 8 POLLEN (200 g) or 3 NECTAR + 3 POLLEN (199 g). Because the CELL is
tilted 30° and elements settle against the back skin, the trigger is essentially a torque
threshold — mass is a good proxy, *and the elements have to actually stay in the CELL*
(so a shot that rattles back out is worse than one that lands soft).

### 2.3 Match start hands you a nearly free TIP
3 of your NECTAR start **inside the upward-facing CELL** (124 g). You also start with **4
pre-loaded POLLEN**. So **3 POLLEN thrown into the CELL ≈ a 20-point TIP in AUTO**, and the
HIVE dumps 3 NECTAR onto the floor for you to recycle. Your AUTO floor is
LEAVE 3 + PARK 5 + TIP 20 = **28 points** with a mechanism you need anyway.

### 2.4 `G407` means you can never tip a HIVE in one trip
Heaviest legal load = 4 × NECTAR = **165 g = 83% of a TIP**. So after the free first TIP,
every TIP costs **two or more trips**. The game is therefore a *trip-count* game:

```
teleop TIPs ≈ (120 s / trip_time) × (grams delivered per trip) / 199 g
```

Measured with `biobuzz/match_model.py` (85% shots land, spilled elements re-collected):

| load per trip | trips per TIP | TIPs in TELEOP (12 s trips) |
|---|---|---|
| 4 POLLEN | 3 | 5.3 |
| 2 NECTAR + 2 POLLEN | 2 | 5.7 |
| 3 NECTAR + 1 POLLEN | 2 | 6.4 |
| 4 NECTAR | 2 | 7.1 (**POLLINATOR 2 RP** territory) |

Trip-time sensitivity (4 POLLEN/trip, one robot): 8 s → 7.4 TIPs; 12 s → 5.3; 18 s → 3.8;
30 s → 2.7. **Going from a 15 s cycle to a 10 s cycle is worth more than any mechanism
upgrade in this document** — about 7 MATCH points per second of trip time at a 12 s cycle. And TIPs are *alliance* points — both robots shoot the same
HIVE — so a partner doing 4 TIPs roughly doubles the alliance total.

### 2.5 The FLOWER is a last-minute, winner-take-all, precision game
* NECTAR may not enter a FLOWER until the **last 60 s** (`G410`, MAJOR FOUL = 20 points to
  the opponent *per NECTAR*).
* Entry is **from the top only**, through a **4.0 in** hole **21.5 in** off the tile
  (`G418`, `9.7`). A 2.8 in POLLEN has 0.6 in of lateral clearance; a 3.6 in NECTAR has
  **0.2 in**. You cannot shoot that. You must *present and drop*.
* Ownership goes to the alliance whose NECTAR is **top-most** — and the owner takes the
  points for **every** element in that FLOWER, including the opponent's. There is also a
  5-point bonus for the **bottom-most** NECTAR.
* Translation: **placing late is worth more than placing a lot**, and a robot that
  dribbles NECTAR into a FLOWER at 1:30 is handing the opponent 20 points.

### 2.6 The cheap points are genuinely cheap
LEAVE 3 + PARK 5 + PARK 5 = **13 points per robot** (and two robots combined satisfy the
SWARM RP threshold of 16). GARDEN elements are **1 point each with no mechanism at all**
(`10.5.3`) — a 23 × 2 in strip you can just push elements into. A robot that never shoots
but parks and pushes 4 elements into the GARDEN still scores ~17.

---

## 3. Subsystem-by-subsystem review

### 3.1 Drivetrain — 15 in × 15 in mecanum

**Grade A−. Keep.**

* There is **no LAUNCH ZONE this season** — `G416` is a *construction* rule, and §9.3
  defines only ALLIANCE AREA, LOADING ZONE and GARDEN. You may shoot from anywhere, so
  "aiming" = rotating the chassis. Mecanum makes
  the shot setup a translation + a rotate instead of an arc — this is the single best
  reason to keep mecanum.
* 15 × 15 is a good compromise: you have 18 × 24 in of legal horizontal envelope (`R105`),
  and you will use the extra 6 in of length for the shooter and the intake nose.
* Guard rails for a small team:
  * Put the battery and the flywheel as low and as close to the drive motors as you can —
    a 20-point TIP machine with a high hood raises your CG, and `G420` only protects you
    from *strategic* tipping, not from physics.
  * Add a belly pan / skirt between the wheels. Loose 2.8 in and 3.6 in balls under a
    mecanum chassis are the classic way to lose a match.
  * Bumper the front intake frame. `G419` explicitly says a "delicately constructed
    mechanism (such as an intake)" is *not* protected — assume you will be hit.

### 3.2 Floating single-shaft gecko intake

**Grade A−. Keep, with one geometry change.**

* This is the same class of device the official 2026-27 goBILDA StarterBot uses
  ("GripForce Gecko Wheel intake… pull POLLEN out of the FLOWER and directly into the
  intake"), so you are not gambling on an unproven concept.
* "Floating" is right — the field is soft foam tile with ±1 in of official tolerance
  (`9.1`) plus real event-to-event variation.
* **Change 1 — the float must be a *squeeze*, not a *hover*.** With 25 g POLLEN the
  dominant failure is the ball sliding *under* the roller, not the roller failing to lift
  it. Bias the float downward with a real spring and limit travel to ≈0.75 in; keep the
  roller's bottom edge roughly 0.3–0.7 in above the tile so a ball cannot fit underneath.
* **Change 2 — give the intake a "nose".** The only clean way to harvest the POLLEN that
  starts inside a FLOWER is the **bottom Retrieval Opening: 3.55 in tall × 3.57 in deep**
  (`9.7`). A full-width bar pressed flat against a 15 in chassis cannot enter that slot.
  Either (a) leave a clear, structure-free nose ~4 in wide where the roller protrudes with
  nothing under or around it, or (b) accept that you only harvest POLLEN from the floor +
  the GARDEN, and treat FLOWER pollen as a bonus. (b) is completely viable: 4 POLLEN per
  GARDEN + 4 pre-loaded + 16 more on the floor.

### 3.3 The tuned wheel spacing — the "unique part" — is the weakest part

**Grade D. Cut it.** Three separate problems, in order of severity.

**(a) The mechanics probably do not do what you think.**
Take two adjacent wheels on one shaft with a clear gap `G = S − w` between their faces
(`S` = centre spacing, `w` = wheel width). Drop a ball of diameter `d` into that gap:

* if `G < d`, the ball is pinched between the two wheel faces → **driven**. Good.
* if `G > d`, the ball passes between the faces — and then **lands on the shaft**. It
  cannot get past a cylinder it is sitting on: it comes to rest on the shaft, *below* the
  wheel rims, where nothing is driving it. Best case the spinning wheel faces fling it
  back out; the more likely case is a ball parked on the shaft, plugging the slot. Either
  way there is no "falls through and rolls out" path unless you build a **second, lower
  collection path** for it — which is the extra mechanism you were trying to avoid.

So "POLLEN that accidentally enters will just roll out" only works if the pollen's exit
route is a different mechanism (a lower roller, a belt, a chute under the shaft) — which is
exactly the extra mechanism your small team is trying to avoid. What you have actually
built at the wide gaps is **a trap that fills with pollen**, which then blocks the slot
for the NECTAR you were trying to grab.

**(b) The tolerance window is thin and the elements are not round.**
Clause `9.8` states plainly that POLLEN and NECTAR "are not perfectly spherical and may
vary in size. Teams should plan for this variation." Your sorting window is
`2.9 in < G < 3.5 in` — about 0.6 in — and both element types are soft polyethylene that
deforms a little under pinch load, which eats further into it.

**(c) The game does not need it.**
* `G407` caps you at **4 elements**. Whatever you sort, you carry four.
* **POLLEN is never bad to have.** It counts for HIVE TIPs, for GARDEN points, and for
  FLOWER points. There is exactly one element you must refuse — **opponent NECTAR**
  (`G408`) — and that is a red-vs-blue problem, not a big-vs-small problem.

**Replacement:** one uniform, deliberately *small* gap with the rule of thumb
**"no gap may ever be wider than the smallest ball, with margin"** — i.e.
`S − w ≤ ~2.5 in`. That guarantees every ball contacts wheel faces and nothing can wedge
in between wheels. Then spend the "sorting" budget on:
1. the **REV Colour Sensor V3** at the intake mouth (classify each ball as it enters), and
2. a **reverse/jog button** so a detected opponent NECTAR gets spat back out immediately.

That is one sensor and one button, versus a rearrangement of the whole intake.

### 3.4 Ramp + second shaft transfer

**Grade B.** It works, and a polycarbonate ramp is a fine material choice (low friction,
drillable, cheap, replaceable). Two concerns:

* The transfer roller and the intake roller will fight each other if their surface speeds
  differ; run the transfer slightly *faster* than the intake, or give it a one-way
  (compliant) behaviour so a backed-up stack cannot push balls forward out of the intake.
* Two motorised shafts = two jam surfaces and one more motor out of eight. A single
  intake roller plus a **passive chute into a gravity-fed 4-element chamber** is fewer
  parts and fewer failure modes; if you find you need help, add the transfer roller later.
* Where balls change direction, keep **≥4.2 in of clearance everywhere except the pinch
  points** — you are moving a 3.6 in ball through a structure designed around a 2.8 in
  ball, and that is where jams live.

### 3.5 Hooded flywheel launcher

**Grade B+.** For *two* element sizes, a hood (a curved back plate with the ball squeezed
between wheel and hood) is a smart choice: a single-wheel launcher's compression is set by
the flywheel-to-hood gap, so a spring-loaded or curved hood will launch a 2.8 in ball and a
3.6 in ball from the same RPM without a rebuild. Dual independent wheels would need a fixed
compression gap — do not do that this season.

**The ballistics you actually need** (do this arithmetic yourself — it is 3 lines):

```
Ball must clear the CELL lip: 53.5 in = 1.36 m above the tile  (§9.6.2)
Launch height (your mechanism) ≈ 0.5 m  ->  rise ≈ 0.9-1.2 m with margin
v_vertical = sqrt(2 · 9.81 · 1.1)    ≈ 4.6 m/s
Launch at ~55°  ->  v_exit = 4.6 / sin(55°) ≈ 5.6 m/s   (~18 ft/s)
Add 25-40% for slip -> plan for v_exit ≈ 6-8 m/s  (20-26 ft/s)
Wheel 3 in (r = 0.038 m):  6 m/s  ->  157 rad/s  ->  ~1500 rpm
Wheel 4 in (r = 0.051 m):  6 m/s  ->  118 rad/s  ->  ~1130 rpm
```

Practical consequences:

1. **You need a fast motor, not a torquey one.** A 312 rpm 5203 through a 3 in wheel is
   ~1.2 m/s — nowhere near enough. Budget for a 5202-series (≈6000 rpm) motor on the
   flywheel with **closed-loop velocity control** (`setVelocity`, run-using-encoder) so
   battery sag does not move your shot, or a 5203 geared up. The energy is trivial
   (≈0.9 J for a NECTAR); **speed** is the constraint.
2. **Do not make it adjustable. Make it repeatable.** Shot accuracy for a 20 × 14 in
   window is a *distance* problem, not an *angle* problem. Fix the hood, pick **one**
   shooting spot per alliance-side and trim the shot with RPM. An adjustable hood is a
   servo, a linkage, a new failure mode and a re-calibration the night before a
   competition, for a target that is enormous.
3. **Shoot so the ball enters descending, not flat.** The CELL mouth is ~20 in wide and
   14 in tall and tilted 30°, with the lip at 53.5 in. A flat line-drive either clips the
   lip or bounces off the back skin. Aim for an apex at or just before the mouth — the
   basketball-free-throw picture.
4. **Do not shoot while the HIVE is tipping.** §10.5.1 warns that LAUNCHING at the
   down-facing CELL during a tip can stop the tip being scored. Add a 1–2 s "settle" hold
   to the shot sequence, and brief your alliance partner about the same.
5. **Rubber choice matters for `G406`.** `G406`/`R201` prohibit routinely gouging or
   tearing SCORING ELEMENTS. Soft urethane/gecko wheels and smooth hood transitions;
   no exposed screw ends in the ball path.

### 3.6 Colour and distance sensors

**Grade A — and they are now mandatory, not a bonus.** `G407` is the reason: you must
know *how many* elements you CONTROL, and the manual explicitly asks teams to design
"systems to prevent active pickup/intaking of more than 4." Your instinct here is the
single best decision in the design brief.

Two refinements:
* Put the **colour sensor at the intake mouth**, where it sees one ball at a time, not in
  the hopper where it sees a pile. Use it to classify POLLEN / own NECTAR / opponent
  NECTAR (`G408`).
* Count with something binary — a **break-beam pair in the indexer** or two ToF sensors
  top/bottom of the chamber. Distance sensors are good at "is a ball staged here?", less
  good at "how many".
* Budget your I2C ports before you buy: the Control Hub has 4 I2C ports total and they do
  not multiply by adding an Expansion Hub. Plan the bus (e.g. colour + 2 ToF on one hub).

### 3.7 Camera

**Grade B+.** Keep it, but change its job:

* The CELL opening is **20 × 14 in** — you do not need vision to *hit* it. You need a
  repeatable *distance* and a repeatable *heading*. Odometry + a chassis rotation does
  that.
* The AprilTag clusters are on the **bottom face of each CELL, facing down at the tiles**
  (`9.9`). They are there for **navigation/localisation** near the HIVE, and they also
  tell you which CELL is up. They are *not* inside the mouth, so do not plan a "tag in the
  hole" aiming pipeline.
* The camera's real value for you: (1) AUTO start alignment, (2) telling you whether the
  HIVE is settled and which CELL is up, (3) auditing your own shot placement in practice.
  The repo you are working in already ships `ConceptAprilTag`, `ConceptAprilTagLocalization`
  and `ConceptVisionColorLocator_Circle` samples under
  `FtcRobotController/.../external/samples/` — start there rather than from scratch.
* Check exposure/lighting early; venue lighting is the number-one cause of "it worked in
  the lab" in this category.

### 3.8 The three-section walled hopper

**Grade D. Cut it.** `G407` caps you at **4 elements controlled at a time**. Three
separated sections can therefore hold at most 4 balls total — and every wall you add is
(1) volume you cannot use, (2) another jam surface, (3) another place a ball can wedge on
a 3.6 in diameter. Replace with:

* **one** chamber, gravity-fed, with a single release gate into the launch pinch,
* a hard mechanical limit so 4 is the maximum you can physically hold, and
* a **"shed" control** (reverse the intake / open the bottom) so that if you ever end up
  with 5 you can get back to 4 in under a second. `G407` is a MAJOR FOUL and a YELLOW CARD
  if STRATEGIC — you want a reflex for it, not a debate.

Keep the *idea* that you can choose what to fire; just do it with **load order** instead
of walls: the gate feeds whatever is in the chamber, and the driver's choice of *what to
pick up* is what controls the mix.

### 3.9 "Later: adjustable hood, then a turret"

**Neither, in that order, and probably not at all.**

* Motor budget (`R503` = 8 motors): 4 drive + 1 intake + 1 flywheel = 6, leaving 2. A
  transfer roller takes one, leaving **1**. A turret (1) + a flower lift (1–2) does not
  fit. A linear slide (1–2 motors) does not fit alongside a shooter.
* A turret buys you nothing: no LAUNCH ZONE means you can shoot from anywhere, and
  mecanum lets you aim by rotating.
* Spend the two spare motors on **(a) the transfer/indexer** and **(b) one single-motor
  flower path later** — or leave one unspent, which is a legitimate design decision for a
  small team (one spare motor = the ability to add a fix at the event).

### 3.10 "All shafts identical"

Good instinct for field repairs — keep it for the **intake and transfer** shafts
(one spare part covers two failures). Two caveats:
* The **flywheel shaft is not like the others**: it spins 5–10× faster, needs to be
  balanced, and carries a different bearing load. Do not force it into the same part.
* Hex shaft in a plastic hub **will slip** under flywheel torque. Use a hub with a set
  screw plus a retaining collar, or a keyed/D-shaft, and Loctite nothing — you need to be
  able to swap it in a pit.

---

## 4. Head-to-head: your design vs the two alternatives

| | **A — yours:** sorter + hooded flywheel + 3-section hopper | **B — even spacing, pollen-only** | **C — slide + claw (+ launcher?)** |
|---|---|---|---|
| Motors | 4 drive + intake + transfer + flywheel = **7–8 of 8** | 4 + intake + transfer + flywheel = **7 of 8** (leave 1 for transfer or drop it) | 4 + intake + flywheel + 1–2 slide = **8–9 of 8 → over budget** |
| Servos | gate, hopper walls, optional | gate = 1–2 | gate + claw + wrist = 3–4 |
| Sort pollen / nectar | mechanical, tolerance-sensitive | mixed (fine) | mixed |
| Shoot NECTAR into HIVE | yes (the design's real strength) | yes | yes (if the launcher survives) |
| Score FLOWERs | **no** — a flywheel cannot hit a 4.0 in hole at 21.5 in | no | **yes — this is the only design that can** |
| Jam risk | high (two sizes, spaced wheels, drop path) | low | medium (balls in a gripper + slide) |
| Build risk for a small team | **high** | **low** | **highest** |
| Typical failure at competition | pollen weeps through the wide gaps, blocks the nectar slots; wrong-colour nectar control | shoots only pollen → 3 trips per TIP instead of 2 | slide dies / over current; claw never gets validated; ends up a pollen-only robot with 2 dead motors |
| Expected alliance TIPs (12 s trips) | ~5.3 | ~5.3 | ~4–6 if built, **0 if not finished** |

**What actually separates A from B:** about **1 TIP per match**, and that gap is closed by
*picking up the right thing on purpose* (drive to your spilled NECTAR) rather than by
separating it mechanically. That is a **software + driver** change, worth more than the
entire sorter.

**What C buys you that A and B cannot:** FLOWER placement. If you want FLOWER points, you
are choosing between (i) building a **single-motor lift/drop** (a cup on a 2-bar arm, or a
small vertical elevator) *instead of* the slide + claw, and (ii) not playing FLOWERs at
all. Remember `G410`: you cannot even put NECTAR in a FLOWER until the last 60 s, and the
last NECTAR in owns the whole FLOWER — so this is a **last-minute, high-variance** play.
For a small team in September, my recommendation is (ii) now, (i) as an October add-on
with the one spare motor; and **never** a claw (`G415`: no grabbing ARENA elements; and
you never need to grip a ball).

### The one-line verdict on the "unique part"

You had one genuinely original idea — **exploit the size difference between POLLEN and
NECTAR inside the intake** — and you spent it on a problem that the rulebook already solved
by capping you at four elements. The size difference *is* exploitable, but the right place
to exploit it is **software and strategy**: NECTAR is 1.65× the mass per element, so 4
NECTAR per trip is 83% of a TIP while 4 POLLEN is 50%. Go get the NECTAR on purpose.

---

## 5. Weak-point / risk register (ranked by points-at-risk × likelihood)

| # | Risk | Why it hurts | Mitigation |
|---|---|---|---|
| 1 | **Cycle time > 15 s** | 5.3 TIPs → 4.4 TIPs; POLLINATOR 2 RP lost | Design for a 10 s trip: short travel, big mouth, no waiting on the flywheel. Practise the 4-ball cycle as a drill. |
| 2 | **Shot not repeatable** | every miss costs ~25 g of a 199 g TIP | One calibrated shooting spot, closed-loop flywheel RPM, 20-shot chart in the pit notebook. |
| 3 | **`G410` FLOWER foul** | MAJOR FOUL per NECTAR = 20 points each to the *opponent* | Hard software interlock: the flower outtake is disabled until the field timer reads ≤60 s. Never make it a driver's judgement call. |
| 4 | **`G407` over-capacity** | MAJOR FOUL + YELLOW CARD if STRATEGIC | Mechanically limit to 4; count with a break-beam; give the driver a "shed" button. |
| 5 | **`G408` opponent NECTAR** | YELLOW CARD if STRATEGIC | Colour sensor at the mouth + reverse. Remember the two HIVEs are only ~25.5 in apart, so after a tip both alliances' NECTAR land in the same neighbourhood. |
| 6 | **Jam at the size change** | a jammed intake in the last 30 s is a lost RP | ≥4.2 in clearance everywhere except the launch pinch; round every ball-path edge; be able to reverse the whole path. |
| 7 | **`G406` ball damage / mess** | MAJOR FOUL, and damaged balls change your calibration | no exposed fastener ends or sharp edges in the ball path; smooth hood entry; check wheels for wear. |
| 8 | **Balls under the robot** | your own chassis becomes a ball trap; `G409` says you may not catch | belly pan, side skirts, don't drive over the spill under the HIVE. |
| 9 | **Motor budget exhausted** | no room for a fix at the event | publish the budget (table in §6) and hold one motor in reserve until October. |
| 10 | **Flywheel browns out the hub** | one bad stall kills your match | separate the flywheel onto its own port; ramp the commanded RPM; check `R505` current limits; carry a spare motor. |
| 11 | **AUTO gives nothing** | 20 points of "free" TIP evaporates | validate "3 of 4 pre-loads into the CELL" as your single AUTO acceptance test. Fallback AUTO = just LEAVE + PARK = 8. |
| 12 | **Getting hit** | intake is explicitly not protected (`G419`) | bumpers, cable-managed wiring, keep the intake nose replaceable (2 bolts, 1 connector). |

---

## 6. Recommended revision — what I would actually build

**Keep:** 15×15 mecanum · floating roller intake · polycarbonate ramp · hooded flywheel ·
camera · intake colour/distance sensors · identical shafts for intake/transfer.

**Change:**

1. **Wheel spacing → uniform and small.** Criterion: `centre_spacing − wheel_width ≤ 2.5 in`
   so no ball can ever enter between wheels. (Test it — see §7.)
2. **Drop the three-section hopper** → one 4-element gravity chamber with **one** gate.
3. **Add a "shed" control** and a break-beam element counter.
4. **Fix the hood; do not add a turret.** Put the spare motors into the transfer roller
   and (optionally, in October) a single-motor flower lift.
5. **Add the colour-reject reflex** (sensor + reverse), aimed squarely at `G408`.
6. **Make the intake nose flower-capable** or consciously give up FLOWER pollen.

**Motor budget (8 of 8):**

| Port | Use | Notes |
|---|---|---|
| 0–3 | 4 × drive | 1:1 with 96–104 mm GripForce mecanum |
| 4 | Intake roller | floating, downward-sprung |
| 5 | Transfer / indexer | slightly faster surface speed than the intake |
| 6 | Flywheel | 5202-class, closed-loop velocity; see §3.5 ballistics |
| 7 | **reserved** | flower lift, or the fix you have not thought of yet |

**Servos (8 available):** release gate (1) · spare/shed (1) · hood trim only if you prove
you need it. Keep servos out of the ball path.

**Geometry targets to lock in CAD:**

* intake roller bottom **0.3–0.7 in** above the tiles, spring travel ≈0.75 in
* ball path clearance **≥4.2 in** everywhere except the launch pinch
* launch exit at **~20–24 in** above the tiles, hood angle ~50–60°
* one shooting spot per alliance side, marked on the tile, ~4–6 ft from the HIVE
* overall: within **18 × 24 × 29 in** at full mechanical extension (`R105`), and 18 in cube
  at the start (`R102`)

**Sequencing (this is the "small team" answer):** build the drivetrain + intake + hopper
first and get a robot that can do **AUTO tip + park + garden** reliably. Add the launcher
second. Add flower capability third, in October, with the spare motor, only after the
first two are boring.

---

## 7. Three tests to run before you commit (a weekend, not a season)

1. **Roller capture rig.** A 12 × 12 in plate, one shaft, a handful of candidate wheel
   spacings (2.0 / 2.5 / 3.0 / 3.5 in gaps). Run 20 POLLEN and 10 NECTAR through it at
   match surface speed. Score: grabbed / jammed / passed-through. You are looking for the
   *widest* gap that still grabs 20/20 POLLEN — that is your spacing, and it should come
   out near or below 2.5 in.
2. **Shot chart.** 20 shots at each of 3 distances and 2 RPMs. Plot makes / short / long /
   off-the-back-skin. This produces the one shooting spot and the one RPM you will use all
   season, and it tells you whether your hood is doing its job on NECTAR as well as
   POLLEN.
3. **The 10-second drill.** Driver + human player, empty field: from a standing start,
   intake 4 elements, drive to the spot, settle, shoot 4, return. Time it. Repeat 20 times.
   If the median is >12 s, do not build another mechanism until it is not.

---

## 8. Rules and facts to re-verify yourself (I am not the source of truth)

* `G304` and the exact STARTING CONFIGURATION / LOADING ZONE staging details (I did not
  read §11.3 in full).
* Field-to-field variation in the HIVE tipping point — the Event Field Setup Guide says
  field staff calibrate each HIVE, so tune your shot to *want* to land, not to *just*
  land (margin, not precision).
* The Q&A / Team Updates for edge cases: several elements entering the CELL together, what
  counts as "CONTROL" of a ball sitting on your robot's top face, and whether a ball that
  rolls in off the floor can ever contribute to a TIP (as written, §10.5.1 says LAUNCHING
  into the upward-facing CELL is the only allowed way — so **design for a launch, not a
  roll-in**).
* The FLOWER scoring volume CAD reference (10-4) if you decide to play FLOWERs.

---

## Appendix A — model output

Generated by `python3 biobuzz/match_model.py`:

```
==============================================================================
1) LOADOUT COMPARISON -- one robot, 12 s trips, hit rate 85%, recapture 85%
   (throughput ceiling: assumes spilled elements are re-collected)
==============================================================================
scenario                       trip/tip  tips  AUTO  TELEOP  MATCH           RP
-------------------------------------------------------------------------------
A  4 POLLEN / trip                    3   5.3    28      96    124        POLL1
B  3 NECTAR + 1 POLLEN                2   6.4     8     137    145        POLL1
C  4 NECTAR / trip                    2   7.1     8     147    155  POLL1+POLL2
D  2 NECTAR + 2 POLLEN                2   5.7     8     128    136        POLL1

==========================================================================
2) TRIP-TIME SENSITIVITY -- 4 POLLEN per trip, ONE robot
==========================================================================
 cycle_s  trips  trip/tip  tips  TELEOP  MATCH           RP
       8     15         3   7.4     141    169  POLL1+POLL2
      10     12         3   6.1     110    138        POLL1
      12     10         3   5.3      96    124        POLL1
      15      8         3   4.4      81    109        POLL1
      18      7         3   3.8      78    106            -
      22      5         3   3.3      58     86            -
      30      4         3   2.7      52     80            -

==============================================================================
2b) SHOT-ACCURACY SENSITIVITY -- 12 s trips, 4 POLLEN per trip
==============================================================================
  hit rate   g/trip   tips  TELEOP  MATCH           RP
       60%       60    3.0      66     74            -
       70%       70    3.5      87     95            -
       80%       80    5.0      86    114        POLL1
       90%       90    5.5     105    133        POLL1
      100%      100    6.0     106    134        POLL1

==========================================================================
3) ALLIANCE TOTAL -- two robots sharing one HIVE (tips add up)
==========================================================================
 cycle_s  tips/robot  tips ALLIANCE   POLL1   POLL2
      10         6.1           10.5     yes     yes
      12         5.3            8.9     yes     yes
      15         4.4            7.3     yes     yes
      18         3.8            6.3     yes      no
      22         3.3            5.3     yes      no
      30         2.7            4.2     yes      no

==============================================================================
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

==============================================================================
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

==============================================================================
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

==========================================================================
4) WHAT A NON-SHOOTING ROBOT IS WORTH (sanity floor)
==========================================================================
scenario                       trip/tip  tips  AUTO  TELEOP  MATCH           RP
-------------------------------------------------------------------------------
LEAVE+PARK+AUTO PARK, no HIVE shots        0   0.0     8       5     13            -
same + 4 elements in GARDEN           0   0.0     8       9     17            -

--------------------------------------------------------------------------
TIP_MASS         199 g  = 8.0 POLLEN or 3 NECTAR + 3 POLLEN
CONTROL_LIMIT    4 elements (G407) -> heaviest load 165 g = 83% of a TIP
TELEOP budget    120 s : trips = 120/cycle_s
NECTAR per trial 3 staged in the CELL + 5 fed in by the human player (G426)
Supply note: at 85% recapture the ALLIANCE loses ~30 g of elements per TIP,
             i.e. ~1.2 POLLEN-equivalents per TIP to be replaced from GARDEN / FLOWER / floor stock.
Take-away: the free first TIP needs only 3 POLLEN (3 NECTAR are already
           in the CELL).  After that a 4-POLLEN load at 85% accuracy needs
           3 trips per TIP while a 3-NECTAR + 1-POLLEN load needs 2, so
           NECTAR + short trips are the two biggest levers you own.
```

**Model caveats.** It is deterministic expected-value bookkeeping, not a rules engine. It
assumes you can re-collect ~85% of the elements a TIP dumps on the floor and replace the
rest from the GARDEN/floor; if your recycling is worse, the tip counts drop. Treat the
numbers as an *upper bound with perfect execution* and use them for **comparisons**, not
for promises.

## Appendix B — one-page cheat sheet for the pit

```
FIRST TIP IS FREE     4 pre-loaded POLLEN; 3 landing = TIP (3 NECTAR already in the CELL)
TIPS COME IN PAIRS    4 elements max (G407) = 165 g max = 83% of the 199 g trigger
NECTAR > POLLEN       41.3 g vs 25.0 g per element; 4 NECTAR = 83% of a TIP, 4 POLLEN = 50%
RPs                   16 combined LEAVE+PARK  |  4 TIPs  |  7 TIPs  (alliance totals)
FLOWERS ARE LAST 60s  G410 - NECTAR in early = 20 points to the opponent, each
FLOWER MOUTH          4.00 in dia at 21.5 in up - drop, do not shoot; top-most NECTAR owns it
DO NOT SHOOT          while the HIVE is tipping (10.5.1) - wait for the settle
CHEAP POINTS          LEAVE 3 + PARK 5 + PARK 5 = 13, and GARDEN elements are 1 each
BUDGET                8 motors / 8 servos (R503); envelope 18 x 24 x 29 in (R105)
```
