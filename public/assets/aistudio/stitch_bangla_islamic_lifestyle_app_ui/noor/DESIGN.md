---
name: Noor
colors:
  surface: '#0f1513'
  surface-dim: '#0f1513'
  surface-bright: '#343b38'
  surface-container-lowest: '#090f0d'
  surface-container-low: '#171d1b'
  surface-container: '#1b211f'
  surface-container-high: '#252b29'
  surface-container-highest: '#303634'
  on-surface: '#dee4e0'
  on-surface-variant: '#bccac0'
  inverse-surface: '#dee4e0'
  inverse-on-surface: '#2b322f'
  outline: '#86948b'
  outline-variant: '#3d4a42'
  surface-tint: '#5fdda5'
  primary: '#5fdda5'
  on-primary: '#003824'
  primary-container: '#14a673'
  on-primary-container: '#003220'
  inverse-primary: '#006c49'
  secondary: '#ffb4ab'
  on-secondary: '#690005'
  secondary-container: '#ce0014'
  on-secondary-container: '#ffdcd8'
  tertiary: '#3edfa5'
  on-tertiary: '#003826'
  tertiary-container: '#00a677'
  on-tertiary-container: '#003221'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#7dfac0'
  primary-fixed-dim: '#5fdda5'
  on-primary-fixed: '#002113'
  on-primary-fixed-variant: '#005236'
  secondary-fixed: '#ffdad6'
  secondary-fixed-dim: '#ffb4ab'
  on-secondary-fixed: '#410002'
  on-secondary-fixed-variant: '#93000b'
  tertiary-fixed: '#63fcc0'
  tertiary-fixed-dim: '#3edfa5'
  on-tertiary-fixed: '#002114'
  on-tertiary-fixed-variant: '#005138'
  background: '#0f1513'
  on-background: '#dee4e0'
  surface-variant: '#303634'
typography:
  headline-xl:
    fontFamily: Plus Jakarta Sans
    fontSize: 40px
    fontWeight: '700'
    lineHeight: 48px
    letterSpacing: -0.02em
  headline-xl-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
    letterSpacing: -0.015em
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 22px
    fontWeight: '600'
    lineHeight: 30px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 26px
  body-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 17px
    fontWeight: '400'
    lineHeight: 26px
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 15px
    fontWeight: '400'
    lineHeight: 22px
  body-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 18px
  label-lg:
    fontFamily: Space Grotesk
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.02em
  label-md:
    fontFamily: Space Grotesk
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.04em
  label-sm:
    fontFamily: Space Grotesk
    fontSize: 10px
    fontWeight: '600'
    lineHeight: 14px
    letterSpacing: 0.06em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-tablet: 1.5rem
  gutter-desktop: 2rem
  margin: 1.25rem
  margin-tablet: 2rem
  margin-desktop: 3rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2.5rem
---

## Brand & Style

The design system establishes a sanctuary-like digital experience rooted in serenity, precision, and mindful practice. It merges modern minimalism with refined atmospheric depth, purposefully avoiding ornate or nostalgic tropes in favor of an ethereal, focused interface. 

The aesthetic conveys spiritual reverence through deep, organic night-sky surfaces, luminous emerald bio-luminescence, and disciplined spatial restraint. The emotional response is one of calm presence, clarity, and unhurried devotion. Key stylistic traits include:

- **Atmospheric Luminosity:** Deep dark-slate tinted surfaces overlaid with selective emerald atmospheric glows that evoke quiet night prayers and dawn transitions.
- **Architectural Discipline:** Generous vertical balance, structured fluid grids, and ultra-crisp typography inspired by contemporary editorial layouts.
- **Organic Softness:** Expansive, soft corner radii (`rounded-2xl` architecture) that soften dense schedules, prayer chronometers, and sacred text cards.

## Colors

The palette is engineered natively for low-light environments, maximizing battery preservation and ocular comfort during dawn (Fajr) and nocturnal (Tahajjud) use.

### Canvas & Surface Hierarchy
- **Base Canvas (`#0d1311`):** Deepest bio-tinted black; grounds the entire viewport.
- **Surface Level 1 (`#121a17`):** Base container elevation for views, panels, and tab arrays.
- **Surface Level 2 (`#18221e`):** Standard resting state for content cards, prayer cards, and interactive rows.
- **Surface Level 3 (`#1f2b26`):** Floating sheets, modals, elevated utility cards, and active states.

### Accents & Indicators
- **Emerald Primary (`#14a673`):** Sacred focal point. Used for core interactive states, current prayer highlights, and active tab states. Often paired with a linear radial ramp into `#2cd49b` for luminous linear indicators and progress halos.
- **Signal Red (`#f02828`):** High-urgency secondary accent. Reserved strictly for Qaza tracking alerts, urgent countdown timers (e.g., Iftar/Imsak final minutes), missed schedules, and dynamic notification pings.

### Typography & Readability
- **Primary Text (`#ffffff`):** Crisp pure white reserved for titles, Quranic romanization, timings, and primary readings.
- **Secondary Text (`#98a7a1`):** Muted cool sage-grey providing sufficient contrast (minimum 4.5:1 against surfaces) for metadata, footnotes, and untargeted prayer times.
- **Subtle Borders (`rgba(20, 166, 115, 0.12)`): Ultra-sheer emerald-infused containment lines to define card edges against deep canvas layers.

## Typography

Typography prioritizes clarity, spaciousness, and modern rhythm:

- **Primary Typeface (Plus Jakarta Sans):** Selected for its geometric cleanliness tempered with warm, open counters. It provides exceptional legibility across prolonged readings (English Quran translations, Hadith chapters, and contextual reflections).
- **Secondary Display & Numeric Typeface (Space Grotesk):** Deployed selectively for metric displays, numerical timetables, countdown digits, Qibla compass degrees, and status tags to impart technical precision and astronomical rigor.

### Hierarchy & Typesetting Guidelines
- Maintain strict line-height discipline to ensure deep dark backgrounds do not visually pinch dense English verse translations.
- Apply title-casing and slight negative tracking (`-0.02em`) on headline ranks above 24px to retain commanding authority without harshness.
- Timers, prayer clocks, and Hijri calendar references must utilize monospace or tabular figure adjustments when rendered with `Space Grotesk` to prevent layout shift during countdown state changes.

## Layout & Spacing

The design system operates on an 8pt spatial grid designed to create breathable, meditative compositions.

### Layout Mechanics
- **Mobile (< 768px):** 4-column fluid layout with an edge margin of `1.25rem` (`margin`) and `1rem` column gutters. Components stack into single-column vertical journeys to focus intention.
- **Tablet (768px - 1024px):** 8-column layout with `2rem` margins. Adapts prayer progress timelines and reading views into dual master-detail configurations.
- **Desktop (> 1024px):** 12-column layout with a fixed max-width container of `1280px` centered within a `3rem` canvas margin, distributing widgets into modular dashboard tiles.

### Spacing Philosophy
- Vertical rhythm must remain uncrowded; content groupings use `space-xl` (`2.5rem`) separations to encourage pauses between reading sections.
- Component padding must scale proportionally with inner contents, reserving `space-lg` (`1.5rem`) for cards to honor the `rounded-2xl` curve boundary without clipping typography.

## Elevation & Depth

Visual depth avoids generic drop shadows, relying instead on tonal layering, luminescence, and glass-inspired transparency:

1. **Surface Tiers:** Content elevates physically closer to the eye via lighter green-tinted charcoal tones: `#0d1311` (base canvas) → `#121a17` (groupings) → `#18221e` (cards) → `#1f2b26` (sheets/modals).
2. **Emerald Atmospheric Glows:** Significant or active states (such as the upcoming Adhan indicator or current prayer card) utilize soft, out-of-focus background radial glows: `box-shadow: 0 0 32px -4px rgba(20, 166, 115, 0.28)`.
3. **Ghost Boundaries:** All raised elements possess a subtle 1px inner rim (`border: 1px solid rgba(255, 255, 255, 0.05)` or `rgba(20, 166, 115, 0.15)` for active nodes) ensuring separation independent of panel contrast.
4. **Frosted Navigation:** Floating headers and bottom toolbars employ backdrop blur filters (`backdrop-filter: blur(16px); background: rgba(18, 26, 23, 0.8)`) allowing underlying content to gracefully glide beneath without harsh geometric seams.

## Shapes

The geometric identity balances architectural precision with serene, comforting curves:

- **Signature Curvature (`rounded-2xl` / `1rem` - `1.5rem`):** Content containers, main prayer tracker blocks, daily verse panels, and interactive bottom sheets utilize continuous 1.5rem (`24px`) radii, providing a soft silhouette against the dark ambient canvas.
- **Micro-Controls:** Standard buttons, badge pills, and input controls carry proportional rounding (`0.75rem` to full pill `9999px`) for touch ergonomics.
- **Consistent Proportions:** Rounded borders never touch screen bounds directly; outer layout margins protect the corner curves, preserving the floating card effect across all device widths.

## Components

### Buttons
- **Primary:** Filled vibrant emerald `#14a673` with `#ffffff` text (`label-lg`). Subtle directional gradient moving toward `#2cd49b` at top-right. Hover/active states project an ambient emerald halo (`0 0 24px rgba(20, 166, 115, 0.35)`).
- **Secondary / Ghost:** Transparent background with surface `#1f2b26` or 1px border `rgba(20, 166, 115, 0.2)`. Pure white text.
- **Alert / Action:** Solid `#f02828` with `#ffffff` typography for high-priority acknowledgments (e.g., stopping active alarms, logging missed prayers).

### Prayer & Verse Cards (`rounded-2xl`)
- Constructed with `#18221e` background, 1px border of `rgba(255, 255, 255, 0.06)`, and `1.5rem` internal padding.
- **Active Prayer Card:** Elevates to `#1f2b26`, with an emerald border accent (`rgba(20, 166, 115, 0.4)`) and a subtle emerald backlight glow bleeding outward. Contains a live countdown rendered in `Space Grotesk`.

### Chips & Badges
- **Status Pills:** Fully rounded (`rounded-full`), height `28px`, internal padding `0.25rem 0.75rem`.
- **Urgent / Timer Badges:** Background `rgba(240, 40, 40, 0.15)` with text `#f02828` and an optional pulsing 6px indicator dot.
- **Standard Metadata:** Background `rgba(20, 166, 115, 0.1)` with text `#2cd49b`.

### Lists & Audio Tracks
- Borderless list rows separated by `space-sm` gaps rather than dividing rules.
- Resting background transparent; pressed/hover states resolve to `#121a17` with smooth 150ms transitions. Active recitation row indicators utilize an emerald vertical pill accent bar along the leading edge.

### Inputs & Search Bars
- Background surface `#121a17`, border 1px `rgba(255, 255, 255, 0.08)`, fully rounded corners (`rounded-xl`).
- Focused state transitions border to `#14a673` with a focused 3px outer atmospheric ring: `rgba(20, 166, 115, 0.15)`. Placeholder text formatted in muted sage `#98a7a1`.

### Specialized Lifestyle Widgets
- **Tasbih (Dhikr) Counter:** Large circular target with concentric ring borders featuring emerald glowing progress arcs and subtle haptic visual ripples upon touch.
- **Qibla Compass HUD:** Dark glass disc surface with micro-notches in `#98a7a1`, an emerald focal sight indicator, and warning states transitioning to `#f02828` when recalibration is required.