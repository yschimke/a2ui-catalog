# Findings: where `material3-a2ui` and the M3 kit disagree

Read off [`docs/evidence/kit-vs-a2ui.png`](evidence/kit-vs-a2ui.png): each mapped kit node beside
the sticker `material3-a2ui` 1.0.0-alpha01 draws for it. These are observations about the LIBRARY's
rendition. This repository cannot change what the library draws; it records the difference so a
library bump that closes it is visible.

| Component | Kit | `material3-a2ui` 1.0.0-alpha01 | Status |
| --- | --- | --- | --- |
| `Tabs` | `Tabs` / Primary: unselected labels in `onSurface`, selected in `primary`. | All three labels in `primary`; only the indicator marks the selection. | Open. Upstream candidate. |
| `Card` | `Card` / Outlined (`52346:27574`). | An `OutlinedCard`. The kit card's slots are A2UI components in the sticker, so their spacing is the Column's rather than the card's. | Expected: A2UI Card has one child. |
| `TextField` | Outlined, with supporting text. | `OutlinedTextField`; the basic catalog has no supporting-text property. | Expected: schema has no such property. |
| `Button` | Leading icon + label. | Label only; A2UI Button has one `child`, which may be a Row of an Icon and a Text. | Expected. |
