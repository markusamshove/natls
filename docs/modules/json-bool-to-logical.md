---
title: "JSON-BOOL-TO-LOGICAL"
slug: json-bool-to-logical
weight: 5
---

The following *function* is needed for parsing JSON strings into Natural variables.

```natural
* >Natural Source Header 000000
* :Mode S
* :CP
* <Natural Source Header
/***********************************************************************
/**
/** Converts a JSON boolean value to Naturals logical value
/** Used by NatGens JSON conversions
/**
/***********************************************************************
DEFINE FUNCTION JSON-BOOL-TO-LOGICAL

RETURNS (L)

DEFINE DATA
PARAMETER
1 #P-JSON-VALUE (A5) BY VALUE /* true or false
END-DEFINE

IF #P-JSON-VALUE = 'true'
  JSON-BOOL-TO-LOGICAL := TRUE
ELSE
  JSON-BOOL-TO-LOGICAL := FALSE
END-IF

END-FUNCTION
END
```
