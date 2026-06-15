---
title: "GET-CURRENT-DECIMAL-CHARACTER"
slug: get-current-decimal-character
weight: 5
---

The following *function* is needed for compressing JSON documents.
NatGen makes sure to set the original DC back to the value it was set.

```natural
* >Natural Source Header 000000
* :Mode S
* :CP
* <Natural Source Header
/***********************************************************************
/**
/** Receive the current character for decimal point notation.
/** The character is saved in the Natural session parameter.
/** Used by NatGens JSON conversions
/**
/***********************************************************************
DEFINE FUNCTION GET-CURRENT-DECIMAL-CHARACTER

RETURNS (A1)

DEFINE DATA
LOCAL
1 #SESSION-PARAMETER (A253/1:4)
1 REDEFINE #SESSION-PARAMETER
2 FILLER 70X
2 #DC (A1)
END-DEFINE

CALLNAT 'USR1005N' #SESSION-PARAMETER(*)

GET-CURRENT-DECIMAL-CHARACTER := #DC

END-FUNCTION
END
```
