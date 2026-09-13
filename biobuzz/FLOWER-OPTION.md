# "I want to score NECTAR too" — should I build a vertical linear slide with a grabber?

Short answer: **no to the grabber, and no to the slide if the nectar is going into the HIVE —
because you don't need either.** But the answer depends on which of two very different jobs you
mean, and they have opposite conclusions.

---

## 0. The direct answer

| You want to... | Do you need a lift? | Right mechanism |
|---|---|---|
| **Score NECTAR in the HIVE** (launch it into the CELL) | **No** | Fix two things on what you already have: the intake aperture/compliance and the flywheel's energy + compression. That is it. |
| **Score NECTAR in a FLOWER** (drop it through the 4.00 in top ring at 21.5 in) | Yes | A **passive cup on a single lift**, released by tipping. **Not** a grabber, and a vertical slide is the harder of your two lift options, not the easier one. |

If you only do one thing: **do the first one.** It is two changes, no new mechanism, no motors,
and it moves you from 3 trips per TIP to 2 trips per TIP, which is worth more than anything else
on your robot (see `DESIGN-REVIEW.md` §2.4 and `match_model.py` §2).

---

## 1. Goal 1 — NECTAR into the HIVE (no slide, no grabber)

Launching NECTAR into the CELL is legal and *better* than POLLEN: 41.3 g versus 25.0 g per
element, against a fixed 4-element limit (`G407`) and a fixed ~199 g tip trigger. 4 NECTAR is
83% of a TIP; 4 POLLEN is 50%. It is the difference between 2 trips per TIP and 3.

Two things stop most pollen-only robots from doing it:

**(a) The intake cannot swallow a 3.6 in ball.** A fixed aperture sized around a 2.8 in ball is
the usual cause. Fix the way `DESIGN-COMPARISON.md` §3 describes: **nominal entry aperture
≥ 4.0 in** + a **spring-loaded roller that closes onto the ball** + **≥ 1.0 in of float travel**
(the 2.8 → 3.6 difference is 0.8 in) + soft compliant wheels + torque-oriented gearing. Entry
and grip are two different requirements; don't solve both with one fixed dimension.

**(b) The flywheel was tuned on the lighter ball.** NECTAR needs ~65% more energy for the same
exit velocity (½mv², 0.45 J vs 0.74 J). Two consequences:

* **Compression.** If the hood gap is sized for a 2.8 in ball, a 3.6 in ball is over-compressed —
  it may stall the wheel, jam, or launch far too fast. A **spring-loaded / compliant hood** keeps
  the compression *fraction* roughly constant across both sizes. This is the single most
  important launcher decision in a two-ball game.
* **RPM droop.** A heavy ball pulls more RPM out of the wheel, so the second and third shots of a
  burst go short. Fix with **more flywheel inertia** (bigger/heavier wheel) and **closed-loop
  velocity** (`setVelocity` + encoder, `RUN_USING_ENCODER`), with a short recovery window
  between shots.

**Acceptance test:** 20 POLLEN + 20 NECTAR, alternating, from the same spot at the same commanded
RPM. If both element types land in the 20 × 14 in CELL window with the same setting, you have a
two-element launcher and you are done. If NECTAR flies long, add a second RPM preset — that is
one button, not one mechanism.

> **Rule notes:** nothing prohibits launching NECTAR at the CELL. `G417` requires that the HIVE
> be moved **only** by launching elements into the **upward-facing** CELL — so aim at the up CELL
> and do not shoot at the down one. `G409` says don't catch elements that a tipping HIVE releases.

---

## 2. Goal 2 — NECTAR into a FLOWER (this one needs a lift)

### 2.1 What the rules actually demand

| Requirement | Source |
|---|---|
| Enter **only through the top ring**: opening **4.00 in dia**, **21.5 in above the tiles** | `9.7`, `G418` |
| 1.25 in tall **backstop** on top to guide elements in | `9.7` |
| NECTAR may not enter a FLOWER until the **last 60 s** — MAJOR FOUL (20 points *to the opponent*) per NECTAR before that | `G410` |
| Owner = **top-most NECTAR**, and the owner scores **every** element in that FLOWER, regardless of who placed it | `10.5.2` |
| **5 points per FLOWER** for the bottom-most NECTAR | Table 10-2 |
| Only POLLEN may be removed (from the bottom retrieval opening) | `G418.B` |
| Grabbing **SCORING ELEMENTS** is allowed; grabbing **ARENA** elements is not; a concave shape wrapping a FLOWER for alignment is explicitly OK | `G415` |

### 2.2 The hard part is the last 0.2 inches

A 3.6 in NECTAR in a 4.00 in hole leaves **0.2 in of clearance per side**. You cannot solve that
with driving accuracy alone, and you cannot shoot it — a ball that has to thread a 4.0 in hole
is not a ballistic problem.

**Do not try to drop it.** The reliable technique is **place-and-withdraw**:

1. Raise the ball until it is resting **on the top ring's rim**, roughly centred — the rim is now
   carrying the ball's weight, so your arm/slide is no longer the alignment reference.
2. Withdraw the cup sideways/backwards so the ball is left sitting on the lip.
3. The ball rolls into the hole, or you nudge it toward the hole with the cup and let the
   **1.25 in backstop** stop it.

This turns a ±0.2 in placement problem into a "get it on the rim and push it home" problem, which
your driving can actually do. **It is also why you must not use a grabber** — see §3.

### 2.3 What about the "just reverse the intake" idea?

The elegant version: **put the intake carriage itself on the lift**, raise it to ~23 in and run
the rollers in reverse so the ball rolls out over the rim. No cup, no gripper, no hand-off, no
servos.

It works, and it avoids a transfer point — but it puts **your single most important mechanism**
(the one that feeds every TIP) on a moving stage with a long stroke. I would not do that on a
small team's first lift. Keep the intake fixed; lift a cup.

---

## 3. Why not a grabber ("gripper at the end of a slide")

A gripper is the right tool when you must **hold** an object against gravity while something else
happens, or when you must place it with your own structure. None of that applies here:

* **You never need to grip a sphere.** A 3-sided **passive cup** carries a 3.6 in ball perfectly.
  A cup has no grip-force calibration, no closing stroke, no servo, no wear, and no drop failure
  mode — it physically cannot let go of the ball until you tip it.
* **Gripper fingers eat the clearance you don't have.** The fingers must open around the ball, so
  the assembly is wider than 3.6 in exactly at the moment it is 0.2 in from the top ring. Every
  finger is a chance to hit the ring or the 1.25 in backstop and knock the ball off the lip.
* **A gripper adds 1–2 servos and a hand-off.** `R503` gives you 8 servos total, and a hand-off
  between the intake and the gripper is a new failure point in the one part of the match you
  cannot retry (the last 60 seconds).
* It is **legal** (`G415` allows grabbing SCORING ELEMENTS) — it is just strictly worse.

**Rule of thumb:** grab things you must *place*; carry things you must *drop*.

---

## 4. Vertical slide vs arm vs elevator

All three need the same thing: get a ball from intake height (~4 in) to ~23 in, i.e. a **stroke of
roughly 17–19 in** if the lift carries it, or ~8 in if you lift from a raised "shelf".

| Option | Motors | Stroke problem | Stowage | Notes |
|---|---|---|---|---|
| **Vertical single-stage slide** | 1 | A single-stage slide gives you roughly its own collapsed length in stroke. 18 in of stroke therefore needs a **2–3 stage cascade** | Good (stows in the 15 in frame) | Cascade = 2–3× the parts, wobble under a cantilevered load, and cable management over the full travel |
| **Pivoting arm + cup** | 1 | No cascade. An 18 in arm pivoting from ~8 in up, at 70°, puts the cup at **~25 in high and ~6 in forward** | Must fold to fit the 18 in starting cube (`R102`) | Fewest parts: 1 motor, 1 pivot, 1 constant-force counterbalance spring |
| **Vertical elevator + cup** (belt/2-stage) | 1 | Same cascade question as the slide, but easier to keep the cup level | Good | Gives a straight-down release, which is the nicest motion for place-and-withdraw |
| ~~Slide + grabber~~ | 1–2 + servos | — | — | See §3 |

**My recommendation:** a **single-motor pivoting arm with a passive cup**, or a **2-stage vertical
elevator** if you prefer a straight vertical release — whichever packages better inside your
15 × 15 frame. Both are one motor. The cascade is the thing to avoid; the grabber is the thing to
avoid absolutely.

**Weight and balance:** whatever you build, the ball at 23 in is a **high, offset mass**. Keep the
battery and flywheel low and central, make the lift's *moving* mass as small as possible (cup +
one stage), and check that you are still stable in `R105`'s 18 × 24 × 29 in envelope with the arm
fully extended — a mechanism that can *mechanically* exceed the envelope is illegal even if the
software limits it.

---

## 5. Is it worth it? The numbers

`match_model.py` §5, straight from the rules:

```
5) FLOWER ECONOMICS -- every FLOWER starts with [4] POLLEN staged in it
   (10.3.1), and staged pollen only pays out to the FLOWER'S OWNER.
   Owner = the ALLIANCE with the top-most NECTAR.  No NECTAR = no owner
   = that flower's pollen is worth 0 to everybody.
==============================================================================
scenario                                    elem  owned  bottom   pts
4 staged POLLEN, nobody places NECTAR          4     no       0     0
+ your 1 NECTAR, opponent never replies        5    yes       1    15
your NECTAR, opponent's NECTAR on top          6     no       0     0
4 FLOWERS x 1 NECTAR each, uncontested        20    yes       4    60
   marginal value in the last 60 s (G410 window):
     FLOWER placement            15 s/cycle  -> 1.00 pts/s   (points only, no RP)
     HIVE tipping, 12 s trips   4 POLLEN  -> 0.56 pts/s   + POLLINATOR RP progress
     HIVE tipping, 12 s trips   4 NECTAR  -> 0.83 pts/s   + POLLINATOR RP progress
   opportunity cost: 165 g of NECTAR diverted to FLOWERS
                     = 0.83 of a TIP (~17 points of hive value forgone)
   the RPs are TIPs only (Table 10-3): POLLINATOR 1 = 4 TIPs, POLLINATOR 2 = 7 TIPs.
   => FLOWERS are the bigger pile of points, TIPs are the RP path.
      Do both, in that order, and never at the cost of the other.
```

**Read that carefully, because it is stronger than I gave flowers credit for in the first review.**
Every FLOWER starts with **4 POLLEN already in it** (`10.3.1`) — 16 POLLEN across the four — and
that staged pollen pays **nobody** until someone owns the flower. One NECTAR per flower flips
15 points; all four flowers is **60 points, three HIVE TIPs**, for four elements and about a
minute of driving.

Three counterweights:

1. **It is contested, and it is last-mover-wins.** The top-most NECTAR owns everything in the
   flower, *including the opponent's elements*. If they place after you, you score **0** for that
   flower — your NECTAR included, which then scores for them. That is why the table's third row
   is a zero, and why you should treat flower placement as a **last-30-seconds** action, not a
   leisurely one.
2. **Flowers earn no RANKING POINTS.** Table 10-3 gives RPs for SWARM (LEAVE + PARK ≥ 16) and
   **POLLINATOR 1 = 4 TIPs / POLLINATOR 2 = 7 TIPs** — all TIPs, all alliance totals. Points win
   the match; TIPs win the ranking. A robot that owns all four flowers and never tips has a
   mediocre event.
3. **The mechanism costs you motors you might want elsewhere** — but only 1 of your 8, if you do
   it the way §4 describes, and you have a spare port in the recommended budget.

**Verdict: build it, but third.** Order of work:

1. Intake that swallows 3.6 in balls (**this is also what lets you tip NECTAR**), fixed hood,
   closed-loop flywheel, AUTO tip → this is your score floor and your RP path.
2. Magazine + 10-second trip discipline → this is what gets you to 7 TIPs.
3. Flower lift, with the spare motor, once 1 and 2 are boring.

---

## 6. If you build it — spec and gates

**Spec**

* **One ball at a time.** One 3.6 in ball is all any flower needs from you; a one-ball cup is
  small, light, and keeps the moving mass down.
* **Passive cup**, 3-sided, open toward the flower, with a **tapered mouth that centres the ball**
  as it leaves. No gripper, no wrist.
* **Release = tip the cup** with one small servo, **or** let a fixed stop on the robot cam the cup
  over as the lift reaches the top of its travel (**0 servos** — try this first).
* **Cup floor at ~23–24 in** at full extension, and the cup able to reach **4–6 in forward** of
  your frame so you can present over the rim while your chassis is clear of the flower's pipes.
* **Software interlock: the flower outtake is disabled until the field timer reads ≤ 60 s**
  (`G410`). A MAJOR FOUL is 20 points to the opponent *per NECTAR* — never leave that to driver
  judgement.
* **Slow is fine.** Placement does not need speed; it needs to not drop the ball.

**Build the flower mock first.** Before you cut a single slide, build a piece of plywood with a
4.00 in hole and a 1.25 in tall backstop, mounted at **21.5 in** above the floor. Test your release
geometry with a NECTAR until it goes 10 for 10. **Design the lift around a release you have
already proved on the bench** — not the other way round. Half of all first flower mechanisms fail
at the release, not at the lift.

**Gate to pass before it goes on the robot:** 5 consecutive successful placements from a normal
driving approach, using a real NECTAR, with the mock at the correct height — then 3 more from the
actual approach line you will use in a match.

---

## 7. Decision tree

```
Do you want NECTAR points in the HIVE?
  -> Do NOT build a lift. Fix the intake aperture/float and the hood compression + RPM loop.
     (2 changes, 0 motors, moves you from 3 trips/TIP to 2.)            <- do this today

Do you want NECTAR points in a FLOWER?
  -> You need a lift; you do not need a grabber.
  -> Is your alliance already reaching 4+ TIPs reliably?
       no  -> fix the HIVE loop first. POLLINATOR RPs beat flower points for ranking.
       yes -> build the mock FLOWER, prove a release 10/10, then build a
              single-motor pivoting arm (or 2-stage elevator) carrying a PASSIVE CUP.
  -> Keep 1 motor in reserve. Never spend your last port on the flower module.
```

**One-line answer:** nectar into the **HIVE** needs no mechanism at all — just an intake that
opens wider and a hood that compresses both ball sizes. Nectar into a **FLOWER** needs a lift,
but a **cup, not a grabber**, and a **single-stage arm or elevator, not a multi-stage slide** —
and it should be the third thing you build, after the hive loop is boring.
