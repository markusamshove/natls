---
title: "LOGICAL-TO-JSON-BOOL"
slug: logical-to-json-bool
weight: 5
---

The following *function* is needed for compressing Natural logical variables
into a JSON document.

```natural
* >Natural Source Header 000000
* :Mode S
* :CP
* <Natural Source Header
/***********************************************************************
/**
/** Converts a Natural logical value to its JSON representation
/** Used by NatGens JSON conversions
/**
/***********************************************************************
DEFINE FUNCTION LOGICAL-TO-JSON-BOOL

RETURNS (A5)

DEFINE DATA
PARAMETER
1 #P-LOGICAL-VALUE (L)
END-DEFINE

IF #P-LOGICAL-VALUE
  LOGICAL-TO-JSON-BOOL := 'true'
ELSE
  LOGICAL-TO-JSON-BOOL := 'false'
END-IF

END-FUNCTION
END
```
