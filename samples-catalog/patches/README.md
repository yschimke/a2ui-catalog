# Patches to the vendored AndroidX samples

A fix to a vendored file is a `NNNN-<what>.patch` here, applied by `scripts/import-samples.mjs`
after every import, with its reason in its own header. A patch that stops applying fails the import,
so a fix cannot silently disappear. There are none today: every imported file compiles and renders
unmodified.
