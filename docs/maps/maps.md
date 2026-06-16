# Maps

Maps are a bit different to the other source modules.

- Multiple programs
- Magic comments

## Multiple Programs

Maps may contain rules, or references to Predict Free Rules.

- [ ] `natls` cannot parse these rules at present and treats their `DEFINE DATA` blocks as errors
- [ ] No idea how to cope with Predict Free Rules

## Rulevar

```
RULEVAR F09CASE.SURNAME
RULEVAR D01CASE.TRANSACTION-TYPE
```

- `RULEVAR` is followed by rule type `F` or `D` and then an index (for that type)
- `F` seems to be "Form rule"
    - Followed by a blank `INCDIC`
    - Then by a rule program inline in the map
- `D` seems to be "Dictionary rule"
    - Followed by an `INCDIC` and a name : Predict free rule
    - Or `INCDIR` and two names : this is injected when a database field is referenced in the map and automatically
    inlines Predict rules defined on that field


## Magic Comments

Less importantly, the comments in Maps contain data, somewhat cryptically encoded in places.

- [ ] Is this just for the map editor in N1 or do they have an effect on how maps run?

### Properties Thing

```
* MAP2: MAP PROFILES *****************************        200***********
* .TTAAAMMOO   D I D I N D I D I        ?_)^&:+(                       *
* 024079        N0NNUCN             X .      01 SYSPROF NR             *
************************************************************************
```

#### Line 2

```
            1         2         3         4         5         6
  0123456789012345678901234567890123456789012345678901234567890123456789
* 024079        N0NNUCNJLHK         X .      01 SYSPROF NR   CONTROLV  *

21 : Filler, Optional partial
22 : Filler, Required partial
23 : Filler, Optional complete
24 : Filler, Required complete
36 : Label Filler
59 : Control variable (A8)

```


## SAG docs

(in error code NAT0721)

INCDIR statements are automatically created if a database field is
included in a map. When the map is catalogued, these instructions will
effect an automatic incorporation of processing rules which might
exist for this field on Predict.

## RULEVAR

- Can inline a program including DEFINE block
- Has access to the top level object defines plus any local ones
