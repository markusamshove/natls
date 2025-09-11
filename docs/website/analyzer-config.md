---
title: "Configuring Analyzers"
slug: analyzer-config
weight: 1
---

## Severity

The code analysis of `natlint` (and therefore `natqube` and `natls`) can be configured through an `.editorconfig` ([editorconfig.org](https://editorconfig.org/)) file.

This makes it possible to configure the severity of diagnostics and pass analyzer specific settings.

Example:

```editorconfig
[*]
natls.NL002.severity = none
```

The severity can be one of:

- `none`: Disables the diagnostic
- `info`
- `warn`
- `error`

## Analyzer configuration

The following configurations can be set in an `.editorconfig` file to configure preferences for analyzers. Note, that **all** configurations are set to `false` by default, so you have to turn them on individually according to the below table.

| Property                                      | Possible values | Description |
|-----------------------------------------------| --- | --- |
| `natls.style.comparisons`                     | `sign`, `short`, `false` | [`NL006`](https://nat-ls.github.io/diagnostics/nl006)|
| `natls.style.disallowtoplevelvars`            | `true`, `false` | [`NL018`](https://nat-ls.github.io/diagnostics/nl018)|
| `natls.style.qualifyvars`                     | `true`, `false` | [`NL019`](https://nat-ls.github.io/diagnostics/nl019)|
| `natls.style.discourage_independent`          | `true`, `false` | [`NL028`](https://nat-ls.github.io/diagnostics/nl028)|
| `natls.style.discourage_gitmarkers`           | `true`, `false` | [`NL030`](https://nat-ls.github.io/diagnostics/nl030)|
| `natls.style.discourage_inlineparameters`     | `true`, `false` | [`NL031`](https://nat-ls.github.io/diagnostics/nl031)|
| `natls.style.discourage_hiddentransactions`   | `true`, `false` | [`NL032`](https://nat-ls.github.io/diagnostics/nl032)|
| `natls.style.discourage_hiddenworkfiles`      | `true`, `false` | [`NL033`](https://nat-ls.github.io/diagnostics/nl033)|
| `natls.style.mark_mainframelongline`          | `true`, `false` | [`NL034`](https://nat-ls.github.io/diagnostics/nl034)|
| `natls.style.discourage_hidden_dbms`          | `true`, `false` | [`NL035`](https://nat-ls.github.io/diagnostics/nl035)|
| `natls.style.discourage_long_literals`        | `true`, `false` | [`NL038`](https://nat-ls.github.io/diagnostics/nl038)|
| `natls.style.discourage_lowercase_code`       | `true`, `false` | [`NL039`](https://nat-ls.github.io/diagnostics/nl039)|
| `natls.style.in_out_groups`                   | `true`, `false` | [`NL041`](https://nat-ls.github.io/diagnostics/nl041/)|

# Example

This `.editorconfig` file configures all comparisons tu use the `sign` style, except for copy codes, which should use the `short` style.

```editorconfig
[*]
natls.style.comparisons=sign
[**/*.NSC]
natls.style.comparisons=short
```
