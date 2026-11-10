---
name: Vibrant Islamic Lifestyle
colors:
  surface: '#f9f9f9'
  surface-dim: '#dadada'
  surface-bright: '#f9f9f9'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f3f3f3'
  surface-container: '#eeeeee'
  surface-container-high: '#e8e8e8'
  surface-container-highest: '#e2e2e2'
  on-surface: '#1b1b1b'
  on-surface-variant: '#3d4a42'
  inverse-surface: '#303030'
  inverse-on-surface: '#f1f1f1'
  outline: '#6d7a71'
  outline-variant: '#bccac0'
  surface-tint: '#006c49'
  primary: '#006c49'
  on-primary: '#ffffff'
  primary-container: '#14a673'
  on-primary-container: '#003220'
  inverse-primary: '#5fdda5'
  secondary: '#006c49'
  on-secondary: '#ffffff'
  secondary-container: '#96f6c6'
  on-secondary-container: '#00734e'
  tertiary: '#c00012'
  on-tertiary: '#ffffff'
  tertiary-container: '#ff574c'
  on-tertiary-container: '#5e0004'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#7dfac0'
  primary-fixed-dim: '#5fdda5'
  on-primary-fixed: '#002113'
  on-primary-fixed-variant: '#005236'
  secondary-fixed: '#96f6c6'
  secondary-fixed-dim: '#7ad9ab'
  on-secondary-fixed: '#002113'
  on-secondary-fixed-variant: '#005236'
  tertiary-fixed: '#ffdad6'
  tertiary-fixed-dim: '#ffb4ab'
  on-tertiary-fixed: '#410002'
  on-tertiary-fixed-variant: '#93000b'
  background: '#f9f9f9'
  on-background: '#1b1b1b'
  surface-variant: '#e2e2e2'
typography:
  display:
    fontFamily: Newsreader
    fontSize: 40px
    fontWeight: '600'
    lineHeight: 48px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Newsreader
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
    letterSpacing: -0.015em
  headline-lg-mobile:
    fontFamily: Newsreader
    fontSize: 26px
    fontWeight: '600'
    lineHeight: 34px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Newsreader
    fontSize: 22px
    fontWeight: '500'
    lineHeight: 30px
  headline-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 18px
  label-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.01em
  label-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.02em
  label-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 10px
    fontWeight: '700'
    lineHeight: 14px
    letterSpacing: 0.04em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-sm: 0.75rem
  margin: 1.25rem
  margin-sm: 1rem
  margin-lg: 1.5rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style

This design system establishes a spiritually resonant, vibrant, and premium mobile experience tailored for contemporary Muslim lifestyles. Blending heritage mindfulness with modern digital design, it centers around high-clarity typography, deliberate spatial breathing room, and luminous green accents.

### Design Aesthetics & Philosophy
- **Modern Sacred Minimalism:** Replaces ornate, heavy traditional filigree with airy layouts, crisp micro-borders, and subtle ambient emerald luminescence.
- **Tone & Mood:** Uplifting, disciplined, serene, and modern. Interfaces should feel welcoming during early morning Fajr reflections and functional during midday schedules.
- **Visual Texture:** Rich dark obsidian and crisp white surfaces paired with sheer emerald gradients (`#14a673` blooming into transparent depth) that evoke the tranquil radiance of dawn and community connection.

## Colors

The palette is engineered around high contrast, spiritual significance, and accessibility. 

- **Primary (`#14a673`):** The signature vibrant emerald. Used for primary CTAs, active prayer times, navigational indicators, and primary visual anchors.
- **Secondary (`#0f7d56`):** Deep rainforest emerald. Serves as gradient termination points, pressed states, and rich container tints.
- **Tertiary / Alert (`#f02828`):** Uncompromising, vivid ruby red. Reserved for critical alerts, fast-breaking countdowns, missed habit notifications, and destructive actions.
- **Neutral Base (`#000000` & `#121413`):** Pure obsidian and deep tinted charcoal provide structural grounding for typography and high-contrast dark card containers.
- **Surface & Canvas (`#ffffff` & `#f7faf8`):** Pure crystalline white surfaces nested over a faint, warm-tinted off-white background to avoid stark eye strain while preserving immaculate clarity.
- **Gradient Formula:** Subtle directional gradients blend `#14a673` at 100% to `#0f7d56` at 85% with an ambient 12% opacity radial flare behind key status cards (e.g., Qibla compasses, upcoming prayer time trackers).

## Typography

The typographic hierarchy builds an intentional tension between classical editorial reverence and contemporary mobile utility.

- **Headlines & Editorial Callouts (`Newsreader`):** An elegant, warm serif with high legibility. Used for surah titles, daily reflections, prayer names, and major milestone headers. It brings an intellectual, poetic gravitas to the digital lifestyle space.
- **Interface, Metrics & Body (`Plus Jakarta Sans`):** A geometric, approachable sans-serif with friendly curves that pairs cleanly with the geometric rounded surfaces. Ensures effortless scanning across prayer schedules, Quran translations, and community feeds.
- **Numbers & Time Displays:** Prayer timings and countdowns leverage `Plus Jakarta Sans` with tabular figures (`tnum`) for clean numerical alignment.

## Layout & Spacing

A mobile-first 4-column fluid layout with dynamic padding systems engineered specifically for hand-held convenience and single-thumb ergonomics.

- **Canvas Margins:** Standardized at `1.25rem` (20px) on mobile viewports, tightening to `1rem` on narrow screens (<360px) and relaxing to `1.5rem` on larger phablet devices.
- **Column Grids:** 4-column fluid mobile grid with `1rem` gutters for feed listings, dual-column prayer cards, and split statistic tiles.
- **Vertical Cadence:** A strict 4px/8px micro-grid governing inner card spacing (`space-md` for standard card bodies; `space-xs` between subtitle and title pairs).

## Elevation & Depth

Depth is defined through crisp perimeter boundaries paired with soft, tinted ambient illumination rather than murky, dark drop shadows.

- **Low-Contrast Micro-Borders:** All raised cards and surfaces feature a 1px solid border (`rgba(0, 0, 0, 0.06)` in light mode, or `rgba(20, 166, 115, 0.2)` when highlighted).
- **Emerald Ambient Glow:** Primary hero elements (e.g., active prayer banner, active audio player) utilize an ambient shadow: `0 10px 30px -8px rgba(20, 166, 115, 0.22)`.
- **Card Layers:** Neutral cards sit flat on `#ffffff` with a crisp border and an ultra-subtle resting elevation: `0 2px 8px -2px rgba(0, 0, 0, 0.04)`. Pressed or active states collapse the elevation slightly while brightening the border to `#14a673`.

## Shapes

The design system embraces smooth, organic curves that feel friendly and tactile.

- **Containers & Cards (`rounded-2xl` / 1rem - 1.5rem):** Large surfaces, cards, modal sheets, and progress meters use fluid 16px to 24px corner radii, echoing subtle Islamic architectural arches without being literal.
- **Controls & Chips:** Interactive chips and badges use pill radii (9999px) for tactile tap targets. Buttons sit comfortably at 16px radius matching card corner curvature.

## Components

### Buttons
- **Primary:** Filled vibrant emerald (`#14a673`) with crisp white label (`Plus Jakarta Sans`, 600 weight). 16px corner radius, 52px minimum height for primary actions. Subtle linear gradient highlight running top-to-bottom.
- **Secondary / Ghost:** Transparent background with 1.5px solid border in `#14a673` and emerald text.
- **Destructive:** Bright crimson alert fill (`#f02828`) with white text, or neutral surface with `#f02828` border for low-tier warnings.

### Prayer & Routine Cards
- Segmented time card modules with crisp borders and 20px corners.
- Active prayer card elevates using an emerald-to-dark-emerald gradient background (`linear-gradient(135deg, #14a673 0%, #0f7d56 100%)`), pure white typography, and a glowing indicator badge.
- Inactive cards use pure white backgrounds with 1px soft gray outlines and dark charcoal text.

### Chips & Badges
- Filter chips (e.g., Quran reciter, topic categories, completed fasts) use 32px height, full pill radius, with 1px border. Selected state fills with subtle 10% emerald tint (`rgba(20, 166, 115, 0.1)`) and solid `#14a673` border and text.

### Inputs & Search
- Inputs feature a 50px height, `#f7faf8` background, 1px border (`rgba(0, 0, 0, 0.08)`), and 16px radius. Focused state transitions smoothly to a 1.5px solid `#14a673` ring with zero shadow offset.

### Tasbih / Habit Counter & Trackers
- Circular tap components featuring high tactile contrast: `#000000` text for counting values, framed by a 3px active emerald ring track and subtle tick feedback.