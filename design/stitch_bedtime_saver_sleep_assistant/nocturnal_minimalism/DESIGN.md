---
name: Nocturnal Minimalism
colors:
  surface: '#131313'
  surface-dim: '#131313'
  surface-bright: '#393939'
  surface-container-lowest: '#0e0e0e'
  surface-container-low: '#1c1b1b'
  surface-container: '#201f1f'
  surface-container-high: '#2a2a2a'
  surface-container-highest: '#353534'
  on-surface: '#e5e2e1'
  on-surface-variant: '#c1c7cf'
  inverse-surface: '#e5e2e1'
  inverse-on-surface: '#313030'
  outline: '#8b9199'
  outline-variant: '#41474e'
  surface-tint: '#93cdfc'
  primary: '#c5e3ff'
  on-primary: '#00344f'
  primary-container: '#90caf9'
  on-primary-container: '#08557e'
  inverse-primary: '#21638d'
  secondary: '#a2d3a4'
  on-secondary: '#0a3817'
  secondary-container: '#24502c'
  on-secondary-container: '#91c193'
  tertiary: '#d1e2ec'
  on-tertiary: '#23333a'
  tertiary-container: '#b5c6d0'
  on-tertiary-container: '#43535b'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#cbe6ff'
  primary-fixed-dim: '#93cdfc'
  on-primary-fixed: '#001e30'
  on-primary-fixed-variant: '#004b71'
  secondary-fixed: '#bdefbe'
  secondary-fixed-dim: '#a2d3a4'
  on-secondary-fixed: '#002109'
  on-secondary-fixed-variant: '#24502c'
  tertiary-fixed: '#d4e5ef'
  tertiary-fixed-dim: '#b8c9d3'
  on-tertiary-fixed: '#0d1e25'
  on-tertiary-fixed-variant: '#394951'
  background: '#131313'
  on-background: '#e5e2e1'
  surface-variant: '#353534'
typography:
  headline-lg:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Inter
    fontSize: 22px
    fontWeight: '600'
    lineHeight: 28px
  title-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '500'
    lineHeight: 24px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.05em
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  base: 8px
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 32px
  gutter: 12px
  margin: 16px
---

## Brand & Style

The design system is centered on the concept of **Visual Hygiene**. It is specifically engineered for late-night usage, prioritizing eye comfort and psychological calm. The aesthetic draws from **Minimalism** and **Modern Corporate** styles, stripping away unnecessary visual stimuli to create a "dimmed" user experience that respects the user's circadian rhythm.

The UI should evoke a sense of quiet reliability. Interaction patterns are intentional and subdued, avoiding erratic animations or aggressive high-contrast transitions. The goal is to provide a "digital sanctuary" that feels professional yet gentle.

## Colors

The palette is strictly low-light. 
- **Primary (#90caf9):** A soft, de-saturated blue used for core actions and active states. It provides legibility without light-bleed.
- **Secondary (#a5d6a7):** A muted sage green reserved for success states, completions, and health-related indicators.
- **Neutral Surface:** We use a tiered charcoal approach. The base background is `#121212`, while primary containers use `#1a1a1a`. 
- **Low-Saturation Accents (#37474f):** Used for non-critical UI elements like inactive icons or secondary borders to keep the visual hierarchy flat and calm.

Avoid pure white (#FFFFFF) for text; use an off-white or light gray (e.g., #E0E0E0) to prevent harsh contrast.

## Typography

This design system utilizes **Inter** for its exceptional legibility at small sizes and its neutral, systematic character. 

To maintain the "bedtime" atmosphere, headline sizes are intentionally capped to prevent large blocks of bright pixels. Letter spacing is slightly tightened on headlines for a compact, modern feel, while labels are given extra breathing room for clarity. Text color should follow a hierarchy: 
- High Emphasis: #E0E0E0
- Medium Emphasis: #9E9E9E
- Disabled/Hint: #616161

## Layout & Spacing

The layout follows a **Fixed-Fluid Hybrid** model optimized for Android handheld devices. It utilizes an 8px base grid to ensure alignment and rhythmic consistency.

- **Grid:** A 4-column grid for mobile, with 16px side margins.
- **Compactness:** Gutters are kept tight (12px) to allow for a higher information density without feeling cluttered.
- **Touch Targets:** Despite the compact visual style, all interactive elements must maintain a minimum 48x48dp touch area to ensure ease of use in low-light environments where motor precision may be reduced.

## Elevation & Depth

This design system avoids traditional heavy shadows. Depth is conveyed through **Tonal Layering** (Surface-Container tiers):

1.  **Level 0 (Base):** #121212 — The main application canvas.
2.  **Level 1 (Cards/Cards):** #1a1a1a — For standard content modules.
3.  **Level 2 (Dialogs/Popups):** #242424 — For elements that require immediate attention.

Where subtle separation is needed, use a **1px low-contrast outline** (#37474f) instead of a shadow. If a shadow is strictly necessary for a floating action button, use a soft, 8px blur with 40% opacity of the background color (not black) to maintain a "glow-free" appearance.

## Shapes

The shape language is disciplined and geometric. 
- **Standard Radius:** All components (buttons, input fields) use a 4px (0.25rem) radius.
- **Container Radius:** Cards and larger surfaces are capped at 8px (0.5rem).

This "Soft" approach provides enough friendliness to feel approachable while maintaining the professional, structured look of a utility app. Avoid pill-shaped buttons or fully circular elements except for small status indicators.

## Components

### Buttons
- **Primary:** Filled with `#90caf9`, text in `#121212`. High visibility for main actions.
- **Secondary:** Outlined with `#37474f`, text in `#90caf9`.
- **Tertiary:** Ghost style, text in `#9E9E9E`.

### Cards
- Background: `#1a1a1a`. 
- Border: 1px solid `#37474f`.
- Corner Radius: 8px. 
- Padding: 16px.

### Input Fields
- Container: `#1a1a1a`. 
- Bottom Border: 2px solid `#37474f`. 
- Focused State: Border changes to `#90caf9`. 
- Labels: Small, medium-emphasis text positioned above the field.

### Chips & Tags
- Used for categories or quick filters. 
- Style: Small 4px radius, `#37474f` background with `#E0E0E0` text.

### Lists
- Dividers: 1px solid `#2c2c2c`. 
- Active State: Subtle background shift to `#242424`.
- Icons: 24dp, tinted with `#90caf9` or `#9E9E9E`.

### Progress Indicators
- Linear bars using `#37474f` as track and `#90caf9` or `#a5d6a7` as the fill. Avoid spinning "loading" animations that are too fast; use a slower, "breathing" pulse for a calmer feel.