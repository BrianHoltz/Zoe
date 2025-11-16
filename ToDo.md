# To Do

## Release Blockers

- Fix javac warnings
- Save/load world
- World > Load Bug > submenu (can't load file in applet)
- Don't worry about Applet
- Option: plankton are only algae
- Sounds:
  - birth, death, new plankton
  - bite, consume
  - mate
- species stats/leaderboard
  - living members
  - living members+descendents
  - living mutants
  - energy
  - kills
  - avg age, size, strength
  - avg # children
  - primogenitor
  - mutation depth

## Priorities

- Belly fullness coloring
    - Find the code for coloring a bug's belly by how full it is. Tell me if there is an efficient alternative technique that displays belly fullness by a "rising water level" instead of a pie slice. What are the mathematics of this?
- When / Do labels that can be called
- Social species
  - gender
  - parent
  - mood: dormant, committed, seeking,
- When code must not have side-effects
- More lisp-like? Difference between Zoel and Lisp is:
- Color replace It.genetics magic
  - allows mimicry, fuzziness
  - birth slightly changes color
- Communication
  - Change Mood to Heap key? $Mood
  - How to communicate status to fellow bugs?
  - Real mimicry
- Zoom in/out, scrolling
- Limit vision to forward angle
- Visually distinguish all randomly-created species:
- Visual indication of how many: ancestors, ancestral species
  - Add a tail to show age of species
  - Add a tail to show # of ancestor individuals/species
- Reflective barriers
- Barf outputs a thorax/egg. small==pollen. evolve 2 genders? hatch counter
- ZSH: Pollen - use Mate to ingest
- ZSH: Scent trails - Me.SmellSomething
- Color of (solid?) head is Mood
- Interworld portal
  - Arriving bugs have no ancestry history
  - Bugs have URL link to home (or previous) world?
  - How to surf worlds? Follow links
  - How to remotely view world? Remote display, limited frame rate
  - Server-ize: separate UI from server
- Load properties from inside web page
  - `InputStream is = this.getClass().getResourceAsStream("/data/file.txt");`
- Bug or species can have inherent speed
- Shade from overlapping bugs, younger shades older
- Metabolism tax (and movement tax?) proportional to diameter^3
- Background bug density controls inflow/outflow of bugs?
- Why does the demo world remember previous pageview's bug counts?
- option to shrink vs. die
- Bug can set its memory of birth/ancestral location
- Brownian momentum vector
- Confidence register
- Launch as Java app from web page: Java Web Start
- select next bug in z-axis stack
- make Operator an interface, use extensible enum
- Library of fittest genes
- gene for photosynthesizing if you're hungry
- aging via random mis-execution
- color stomach contents, toggle this view on/off
- multi-turn pain
- night
- Move N
- dump species stats periodically
- gaming
  - paste bugs/joules
  - write bug to play tag
  - naming bugs
  - sounds
- commands
  - extinct/die species
    - highlight
    - children
    - species
    - descendents
  - select bug
    - oldest
    - most kills
    - by ID
    - highest generation
    - most children
    - most descendents
    - parent
  - "Select leader"?
- algae/sunlight/temperature gradients
- Action VM reuse When VM to reuse its stack
- programming
  - Stack.0, Heap.Key
  - labels only on StatementList ( ExpressionList )
  - Call looks for in-gene label, then asks host to look it up
  - GeneCallStack instead of a PheneList, allowing re-entrant calls to Genes
- science
  - set sampling frequency, to run experiments without GUI
  - window onto larger world
  - photosynthesis sharing in the same stack
- scent: each particle is a stack datum and a pointer to author
- attach/detach: colonies, parenting
- hash by x,y

- Memory use
  - 293MB 2.8M cycles 27 of 40K bugs 11h

# Experimental Results

- We hand-coded Vulture and Algae species, but the Vulture species spawned a mutant that is more algae-like than our Algae species.
- Another Vulture mutant lost its Gene sequence for fleeing from living organisms, with the result that when two of them encounter each other they freeze and in effect set up a colony.
- Nursing
- Parasitism
- Convergent evolution: under parameters in which Spiders took over, in the next universe both Mosquitoes and Clams evolved into spiders and took over the world

# Git Tasks

2014-03-24 19:49:15 PDT World.html — deleted (legacy applet wrapper removed)
2015-05-09 14:34:54 PDT applet.html — legacy HTML applet wrapper (unchanged/legacy)
2015-12-05 11:12:45 PST todo.html — content/markup edits (documentation/todo)
2015-12-05 18:07:26 PST src/org/holtz/zoe/zoel/ExpressionListCall.java — interpreter/zoel source modified
2015-12-05 18:07:26 PST src/org/holtz/zoe/zoel/Literal.java — interpreter/zoel source modified
2015-12-05 18:07:26 PST src/org/holtz/zoe/zoel/Number.java — interpreter/zoel source modified
2015-12-05 18:07:26 PST src/org/holtz/zoe/zoel/Operation.java — interpreter/zoel source modified
2015-12-05 18:07:26 PST src/org/holtz/zoe/zoel/OperationCall.java — interpreter/zoel source modified
2015-12-05 18:08:28 PST src/org/holtz/zoe/zoel/Operator.java — interpreter/zoel source modified
2015-12-05 18:09:25 PST src/org/holtz/zoe/zoel/RegisterReference.java — interpreter/zoel source modified
2015-12-05 18:10:58 PST src/org/holtz/zoe/zoel/Stack.java — interpreter/zoel source modified
2015-12-05 18:11:13 PST src/org/holtz/zoe/zoel/StringLiteral.java — interpreter/zoel source modified
2015-12-05 18:11:18 PST src/org/holtz/zoe/zoel/Value.java — interpreter/zoel source modified
2015-12-05 18:11:43 PST src/org/holtz/zoe/zoel/ZoelVM.java — interpreter/VM source modified
2015-12-05 18:12:32 PST src/org/holtz/zoe/Gene.java — genetics-related source modified
2015-12-05 18:13:24 PST src/org/holtz/zoe/zoel/ZoelVMHost.java — zoel host interface modified
2019-03-02 13:40:05 PST Zoe.properties copy — untracked copy of properties file (legacy)
2022-02-21 22:45:04 PST src/org/holtz/zoe/zoeswing/BugIcon.java — new rendering helper added (stomach-level paint)
2023-02-01 07:41:58 PST Zoe/ (directory) — packaged artifacts / zip contents (untracked)
2025-11-15 15:54:06 PST README.md — recent README updates (build/run instructions)
2025-11-15 15:54:24 PST build.sh — build script edits
2025-11-15 16:01:19 PST OPTIMIZATIONS_APPLIED.md — new/edited optimization notes
2025-11-15 16:10:43 PST src/org/holtz/zoe/World.java — recent core world changes
2025-11-15 16:10:57 PST src/org/holtz/zoe/ZObject.java — recent core object changes
2025-11-15 16:11:07 PST src/org/holtz/zoe/Bug.java — recent Bug class edits
2025-11-15 16:11:13 PST PERFORMANCE_ANALYSIS.md — performance notes/analysis added
2025-11-15 16:20:37 PST ToDo.md — updated TODO / planning document
2025-11-15 17:13:49 PST docs/ — recently added/updated docs (BellyFullness.md and assets)