---
name: Civic Vanguard
colors:
  surface: '#f8f9ff'
  surface-dim: '#cbdbf5'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4ff'
  surface-container: '#e5eeff'
  surface-container-high: '#dce9ff'
  surface-container-highest: '#d3e4fe'
  on-surface: '#0b1c30'
  on-surface-variant: '#45464d'
  inverse-surface: '#213145'
  inverse-on-surface: '#eaf1ff'
  outline: '#76777d'
  outline-variant: '#c6c6cd'
  surface-tint: '#565e74'
  primary: '#000000'
  on-primary: '#ffffff'
  primary-container: '#131b2e'
  on-primary-container: '#7c839b'
  inverse-primary: '#bec6e0'
  secondary: '#006a61'
  on-secondary: '#ffffff'
  secondary-container: '#86f2e4'
  on-secondary-container: '#006f66'
  tertiary: '#000000'
  on-tertiary: '#ffffff'
  tertiary-container: '#002114'
  on-tertiary-container: '#069669'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dae2fd'
  primary-fixed-dim: '#bec6e0'
  on-primary-fixed: '#131b2e'
  on-primary-fixed-variant: '#3f465c'
  secondary-fixed: '#89f5e7'
  secondary-fixed-dim: '#6bd8cb'
  on-secondary-fixed: '#00201d'
  on-secondary-fixed-variant: '#005049'
  tertiary-fixed: '#85f8c4'
  tertiary-fixed-dim: '#68dba9'
  on-tertiary-fixed: '#002114'
  on-tertiary-fixed-variant: '#005137'
  background: '#f8f9ff'
  on-background: '#0b1c30'
  surface-variant: '#d3e4fe'
  municipal-navy: '#0F172A'
  slate-surface: '#1E293B'
  teal-accent: '#0D9488'
  emerald-active: '#059669'
  canvas-base: '#F8FAFC'
  card-surface: '#FFFFFF'
  surface-subtle: '#F1F5F9'
  border-refined: '#E2E8F0'
  citation-highlight: '#CCFBF1'
  warning-amber: '#D97706'
  error-crimson: '#DC2626'
typography:
  headline-xl:
    fontFamily: Plus Jakarta Sans
    fontSize: 40px
    fontWeight: '700'
    lineHeight: 48px
    letterSpacing: -0.02em
  headline-xl-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 30px
    fontWeight: '700'
    lineHeight: 38px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.015em
  headline-lg-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  title-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-md:
    fontFamily: Inter
    fontSize: 15px
    fontWeight: '400'
    lineHeight: 24px
  body-sm:
    fontFamily: Inter
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 20px
  label-md:
    fontFamily: Inter
    fontSize: 13px
    fontWeight: '600'
    lineHeight: 18px
    letterSpacing: 0.01em
  label-sm:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.04em
  code-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 18px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1.5rem
  gutter-mobile: 1rem
  margin: 2rem
  margin-mobile: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2.5rem
---

## Brand & Style

This design system establishes an authoritative, crystal-clear, and welcoming digital interface tailored for municipal intelligence, policy transparency, and multilingual civic access. It bridges institutional governance and human-scale legibility. The emotional response aims to convey absolute institutional integrity, accountability, and effortless accessibility—countering the intimidation, dense jargon, and opacity often associated with public sector documentation.

The aesthetic blends **Corporate / Modern Civic Precision** with **Functional Scandinavian Utility**:
- **Tone**: Authoritative, unbiased, hyper-legible, empathetic, and organized.
- **Visual Tenets**:
  - High structural clarity using precise bounding surfaces and quiet, low-contrast structural dividers rather than heavy ornament.
  - Distinct data provenance markers: citations, page references, and OCR verification layers receive dedicated badge geometry and high-legibility typographic treatments.
  - Tactile clarity: interactive affordances (such as language selectors, document drawers, and search inputs) are explicit, satisfying WCAG AAA touch targets and contrast specifications.
  - Subdued, trustworthy base canvases punctuated by energetic civic teal and emerald accents to signal verified facts, operational status, and active processing states.

## Colors

The palette is engineered to meet rigorous accessibility standards (WCAG AAA for primary content, AA for badges and secondary indicators) while preserving visual hierarchy across complex legal, analytical, and multilingual interfaces.

- **Primary (`#0F172A` - Municipal Navy)**: Represents the grounding authority of institutional governance. Applied across primary headings, major interface anchors, primary button states, and deep shell containers.
- **Secondary (`#0D9488` - Civic Teal)**: Functions as the energetic civic focal point. Used for interactive states, key callouts, citation highlights, and active document processing indicators.
- **Tertiary (`#059669` - Emerald Active)**: Denotes verified ground-truth statements, successful OCR indexing, validated provenance, and actionable citizen steps.
- **Neutral (`#64748B` - Slate Core)**: Supplies balanced mid-tones for secondary text, metadata labels, search placeholders, and structural dividers.
- **Surfaces**:
  - Main Canvas: `#F8FAFC`
  - Elevated Cards & Panes: `#FFFFFF`
  - Table Rows, Inset Provenance Containers, & Toolbars: `#F1F5F9`
- **Functional & Named Colors**:
  - `citation-highlight` (`#CCFBF1`): Backing tone for sentence-window references retrieved from the SQLite FTS5 database.
  - `border-refined` (`#E2E8F0`): Clean, hairline structural divider across all panels and tables.

## Typography

Typography pairs **Plus Jakarta Sans** for prominent interface titles, section headers, and data metrics with **Inter** for sustained document readability, citations, multilingual translations, and data labels.

- **Plus Jakarta Sans** injects a modern, accessible, and civic-minded geometric warmth into high-level navigation, document titles, and major queries.
- **Inter** provides neutral, systematic clarity for complex legal texts, extracted OCR outputs, paragraph excerpts, and granular toolbars.
- **Readability & Tracking**:
  - Headings feature tight, deliberate negative letter-spacing (`-0.01em` to `-0.02em`) to maintain composure at large scales.
  - Labels and provenance tags leverage slight positive tracking (`+0.01em` to `+0.04em`) to ensure legibility in all-caps or compact badge presentations.
- **Multilingual Support**: Inter ensures universal script compatibility and predictable vertical metrics across multilingual translation displays (e.g., Hindi, Spanish, Vietnamese) without breaking container bounds.

## Layout & Spacing

The layout is built upon an 8pt architectural rhythm using a **12-column responsive fluid grid** with strict max-width boundaries for reading ergonomics.

- **Breakpoints**:
  - **Mobile (< 640px)**: 4-column layout, `1rem` outer canvas margin, `1rem` column gutter. Panels and translation drawers stack vertically.
  - **Tablet (640px - 1024px)**: 8-column layout, `1.5rem` outer margin, `1.25rem` gutter. Split side-by-side view for document viewer and source query results.
  - **Desktop (≥ 1024px)**: 12-column layout, `2rem` outer canvas margin, `1.5rem` gutter. Fixed global sidebar (280px) paired with a 2-column or 3-column split-pane workflow (Document Source Panel + Civic AI Dialogue/OCR Translation Canvas).
- **Rhythm Guidelines**:
  - `space-xs` (4px) and `space-sm` (8px): Used strictly for micro-spacing, internal chip padding, icon-to-label offsets, and citation breadcrumb gaps.
  - `space-md` (16px): Standard gap between form fields, list rows, and internal card content segments.
  - `space-lg` (24px): Padding within primary data cards, modals, policy document blocks, and query result cards.
  - `space-xl` (40px): Section breaks between query modules, document analytics, and administrative upload sections.

## Elevation & Depth

This system avoids dramatic skeuomorphic depth in favor of **structural containment, tonal stratification, and whispered ambient shadows**.

- **Surface Tiers**:
  - `Level 0 (Canvas Base)`: Flat `#F8FAFC`. Houses the application layout frame and background.
  - `Level 1 (Card & Section Containers)`: Solid `#FFFFFF` backed by a 1px border of `#E2E8F0` and an ultra-subtle ambient shadow: `0 1px 3px 0 rgba(15, 23, 42, 0.04), 0 1px 2px -1px rgba(15, 23, 42, 0.02)`.
  - `Level 2 (Interactive Floating Tools & Overlays)`: OCR toolbars, contextual citation popovers, and sticky translation controls: `#FFFFFF` with a 1px border of `#CBD5E1` and an elevated shadow: `0 4px 6px -1px rgba(15, 23, 42, 0.07), 0 2px 4px -2px rgba(15, 23, 42, 0.05)`.
  - `Level 3 (Modal Dialogues & Full Page Drawers)`: `#FFFFFF` bounded by an ambient overlay: `0 20px 25px -5px rgba(15, 23, 42, 0.12), 0 8px 10px -6px rgba(15, 23, 42, 0.06)`, with a dark scrim tinted at `rgba(15, 23, 42, 0.45)` with `backdrop-filter: blur(4px)`.
- **Inset Provenance Depth**:
  - Sentence-level citation anchors and OCR bounding boxes use tonal inset surfaces (`#F1F5F9`) paired with left-accent borders (`3px solid #0D9488`) rather than drop shadows.

## Shapes

The shape system adopts a **Level 2 (Rounded)** curvature profile: balanced, friendly, and structured.

- **Base Radius (`0.5rem` / 8px)**: Applied to input elements, toolbars, buttons, citation tags, and table cells.
- **Container Radius (`rounded-lg` / `1rem` / 16px)**: Applied to main panels, document cards, modal viewports, and chat speech/answer frames.
- **Large Radius (`rounded-xl` / `1.5rem` / 24px)**: Applied to major layout regions and sticky bottom-sheet translators.
- **Pill Geometry (`rounded-full`)**: Strictly reserved for status badges (e.g., "Indexed", "Page 4, ¶ 2"), OCR language chips, and category tags to differentiate them from actionable rectangular buttons.

## Components

### Buttons
- **Primary**: Background `#0F172A`, text `#FFFFFF`, radius `0.5rem`, padding `0.625rem 1.25rem`. Hover: `#1E293B`. Focus: `2px` ring `#0D9488` with `2px` offset.
- **Secondary / Action Accent**: Background `#0D9488`, text `#FFFFFF`, radius `0.5rem`. Hover: `#0F766E`. Active: `#115E59`.
- **Subtle / Ghost**: Background `transparent`, text `#0F172A`, border `1px solid #E2E8F0`. Hover: background `#F1F5F9`, border `#CBD5E1`.

### Citation Provenance Cards & Badges
- **Provenance Citation Card**: Bounded by `#E2E8F0`, surface `#FFFFFF`. Includes a left accent bar (`3px solid #0D9488`). Inside:
  - Header with `label-sm` font, tracking `0.04em`, displaying `DOCUMENT NAME • PAGE 3, PARA 2`.
  - Body excerpt rendered in `body-sm` (`#1E293B`) on a background `#F8FAFC` inset box.
  - Verification chip (`rounded-full`, bg `#CCFBF1`, text `#0F766E`, icon `CheckCircle`).
- **Citation Reference Chips**: Inline tags within the AI response (e.g., `[Doc 1, p.4]`). Clickable, `rounded-full`, padding `2px 8px`, font `label-sm`, background `#E2E8F0`, hover background `#CCFBF1`, active border `#0D9488`.

### Multilingual OCR Translation Toolbar
- **Toolbar Container**: Sticky header or floating bottom toolbar. Surface `#FFFFFF` with border `#E2E8F0`, elevated shadow Level 2, radius `0.75rem`.
- **Language Switcher**: Segmented toggle or dropdown with source language indicator (`Auto-detected (OCR)` in `#64748B`) and target language picker with quick-swap icon.
- **Jargon Dissector Pill**: Highlights municipal terminology (e.g., *ad valorem*, *easement*, *statutory exemption*). Clicking summons a localized popover with plain-language definitions.

### Inputs & Document Dropzones
- **Search & Question Bar**: Large input (`height: 52px`), radius `0.75rem`, surface `#FFFFFF`, border `1.5px solid #E2E8F0`. Left icon in `#64748B`. Focus: border `#0D9488`, ring `3px rgba(13, 148, 136, 0.15)`.
- **Document Dropzone**: Dashed border (`2px dashed #CBD5E1`), background `#F8FAFC`, radius `1rem`, padding `2.5rem 1.5rem`. Active drag state: background `#F0FDFA`, border `#0D9488`. Displays allowed formats: `PDF, PNG, JPG, JPEG`.

### Checkboxes, Radios, & Status Indicators
- **Checkbox / Radio**: Border `1.5px solid #94A3B8`, radius `0.25rem` (checkbox) or `rounded-full` (radio). Checked state: fill `#0D9488`, checkmark `#FFFFFF`. Focus ring `#0D9488`.
- **Indexing Status Pill**:
  - Processing: Background `#FEF3C7`, text `#92400E` (pulse dot).
  - Grounded / Ready: Background `#DCFCE7`, text `#166534`.