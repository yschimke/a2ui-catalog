#!/usr/bin/env node
/**
 * Project `ui-builder.policy.json`'s `builtins` from `a2ui-basic-catalog.schema.json`.
 *
 * An A2UI catalog has no Compose call site per component — every component is a protocol type the
 * message processor routes to — so none of them can be in the discovery record, and every one is a
 * builder BUILTIN. What a builtin may carry (its properties, its slots) is exactly what the
 * catalog's own JSON Schema says an agent may send, and that schema is committed and held to the
 * library by `CatalogSchemaTest`. So the builtins are GENERATED from it: a library bump that adds a
 * property moves the schema, this projection, and the builder palette together, in one diff.
 *
 * The hand-written part is the header of `ui-builder.policy.json` (everything but `builtins`) and
 * the small tables below — which structural role each component plays and which of its properties
 * are child references (slots) rather than values — because the schema cannot say either.
 *
 *     node scripts/ui-builder-policy.mjs          # rewrite ui-builder.policy.json's builtins
 *     node scripts/ui-builder-policy.mjs --check  # CI: fail if they are stale
 */
import { readFileSync, writeFileSync } from "node:fs";

const SCHEMA = "a2ui-basic-catalog.schema.json";
const POLICY = "ui-builder.policy.json";

/** The template role, shelf and group each basic-catalog component takes. */
const SHAPE = {
  Row: { role: "container", shelfRole: "Container", group: "Layout" },
  Column: { role: "container", shelfRole: "Container", group: "Layout" },
  List: { role: "list", shelfRole: "Container", group: "Layout" },
  Card: { role: "container", shelfRole: "Container", group: "Containers" },
  Tabs: { role: "container", shelfRole: "Container", group: "Containers" },
  Modal: { role: "overlay", shelfRole: "Container", group: "Containers" },
  Text: { role: "decoration", shelfRole: "Leaf", group: "Content" },
  Icon: { role: "decoration", shelfRole: "Leaf", group: "Content" },
  Divider: { role: "decoration", shelfRole: "Leaf", group: "Content" },
  Image: { role: "decoration", shelfRole: "Leaf", group: "Media" },
  Video: { role: "decoration", shelfRole: "Leaf", group: "Media" },
  AudioPlayer: { role: "decoration", shelfRole: "Leaf", group: "Media" },
  Button: { role: "controlled", shelfRole: "Container", group: "Actions" },
  TextField: { role: "controlled", shelfRole: "Leaf", group: "Fields" },
  CheckBox: { role: "controlled", shelfRole: "Leaf", group: "Fields" },
  ChoicePicker: { role: "controlled", shelfRole: "Leaf", group: "Fields" },
  Slider: { role: "controlled", shelfRole: "Leaf", group: "Fields" },
  DateTimeInput: { role: "controlled", shelfRole: "Leaf", group: "Fields" },
};

/**
 * Properties that hold child component IDS, and so are slots in the builder rather than values.
 * `ChildList` is the schema's "array of ids, or a template"; `ComponentId` is a single id.
 */
const SLOT_REFS = {
  "common_types.json#/$defs/ChildList": { max: undefined },
  "common_types.json#/$defs/ComponentId": { max: 1 },
};

/** A JSON Schema property → the builder's `jsonType`. `$ref`s to dynamic values keep their shape. */
function jsonTypeOf(schema) {
  if (schema.type) return Array.isArray(schema.type) ? schema.type[0] : schema.type;
  const ref = schema.$ref ?? "";
  if (/Dynamic(String)$/.test(ref)) return "string";
  if (/DynamicNumber$/.test(ref)) return "number";
  if (/DynamicBoolean$/.test(ref)) return "boolean";
  if (/DynamicStringList$/.test(ref)) return "array";
  return "object";
}

export function project(schema) {
  const builtins = {};
  for (const [name, component] of Object.entries(schema.components)) {
    const shape = SHAPE[name];
    if (!shape) throw new Error(`${SCHEMA} names ${name}, which scripts/ui-builder-policy.mjs has no role for`);
    const required = new Set(component.required ?? []);
    const slots = {};
    const properties = [];
    for (const [key, prop] of Object.entries(component.properties ?? {})) {
      if (key === "component") continue;
      const slot = SLOT_REFS[prop.$ref ?? ""];
      if (slot) {
        slots[key] = {
          acceptedRoles: ["Container", "Leaf"],
          acceptedTraits: ["A2uiComponent"],
          ...(required.has(key) ? { required: true } : {}),
          ...(slot.max ? { max: slot.max } : {}),
        };
        continue;
      }
      const entry = { name: key, jsonType: jsonTypeOf(prop), required: required.has(key) };
      if (prop.enum) entry.allowedValues = prop.enum;
      const notes = [prop.description, prop.$ref ? `A2UI type: ${prop.$ref.split("/").pop()}` : null]
        .filter(Boolean)
        .join(" ");
      if (notes) entry.notes = notes;
      properties.push(entry);
    }
    builtins[`a2ui/${name}`] = {
      role: shape.role,
      shelfRole: shape.shelfRole,
      displayName: name,
      group: shape.group,
      canvas: "placeholder",
      traits: ["A2uiComponent"],
      ...(Object.keys(slots).length ? { slots } : {}),
      properties,
    };
  }
  return builtins;
}

function main(argv) {
  const schema = JSON.parse(readFileSync(SCHEMA, "utf8"));
  const policy = JSON.parse(readFileSync(POLICY, "utf8"));
  const next = { ...policy, builtins: project(schema) };
  const text = `${JSON.stringify(next, null, 2)}\n`;
  if (argv.includes("--check")) {
    if (readFileSync(POLICY, "utf8") !== text) {
      console.error(`${POLICY}'s builtins are stale against ${SCHEMA}. Run node scripts/ui-builder-policy.mjs.`);
      process.exit(1);
    }
    console.log(`${POLICY} matches ${SCHEMA}.`);
    return;
  }
  writeFileSync(POLICY, text);
  console.log(`Wrote ${Object.keys(next.builtins).length} builtins to ${POLICY}.`);
}

if (process.argv[1] && process.argv[1].endsWith("ui-builder-policy.mjs")) main(process.argv.slice(2));
