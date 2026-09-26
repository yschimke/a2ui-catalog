package ee.schimke.a2uicatalog.harness

/**
 * The eighteen components of the A2UI basic catalog v0.9.1, in the order `A2uiBasicCatalogV1`
 * declares them. The catalog's own inventory test holds the sticker sheet to this list, so a
 * component the library ships cannot be missing from the sheet without a failing build.
 */
val BasicCatalogComponents: List<String> =
  listOf(
    "Text",
    "Image",
    "Icon",
    "Video",
    "AudioPlayer",
    "Row",
    "Column",
    "List",
    "Card",
    "Tabs",
    "Modal",
    "Divider",
    "Button",
    "TextField",
    "CheckBox",
    "ChoicePicker",
    "Slider",
    "DateTimeInput",
  )
