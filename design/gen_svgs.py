#!/usr/bin/env python3
"""Generates dark-theme SVG mockups (390x844) of the main Ruhani screens.
Colors come from app/src/main/java/com/example/ui/theme/Color.kt. Run: python3 gen_svgs.py"""
import os

EM, RED, GOLD = "#14A673", "#F02828", "#F5B041"
BG, S1, S2, S3, OUT = "#000000", "#07130E", "#0D2119", "#133024", "#1C4D39"
TXT, TXT2, MINT = "#FFFFFF", "#B8D8CB", "#38D399"
W, H = 390, 844
FONT = "font-family='Inter,Segoe UI,Roboto,sans-serif'"


def esc(s):
    return s.replace("&", "&amp;").replace("<", "&lt;")


def rect(x, y, w, h, fill=S1, r=16, stroke=OUT, sw=1, op=1):
    st = f" stroke='{stroke}' stroke-width='{sw}'" if stroke else ""
    return f"<rect x='{x}' y='{y}' width='{w}' height='{h}' rx='{r}' fill='{fill}' fill-opacity='{op}'{st}/>"


def text(x, y, s, size=13, fill=TXT, weight=400, anchor="start"):
    return (f"<text x='{x}' y='{y}' font-size='{size}' fill='{fill}' font-weight='{weight}' "
            f"text-anchor='{anchor}' {FONT}>{esc(s)}</text>")


def circle(cx, cy, r, fill=EM, stroke=None, sw=1):
    st = f" stroke='{stroke}' stroke-width='{sw}'" if stroke else ""
    return f"<circle cx='{cx}' cy='{cy}' r='{r}' fill='{fill}'{st}/>"


def pill(x, y, w, label, fill=EM, fg=TXT, h=20):
    return rect(x, y, w, h, fill, h // 2, None) + text(x + w / 2, y + h / 2 + 4, label, 10, fg, 700, "middle")


def card(x, y, w, h, hl=False):
    return rect(x, y, w, h, S1, 18, EM if hl else OUT, 1.2 if hl else 1)


def icon_box(x, y, s=40, fill=EM, op=0.18):
    return rect(x, y, s, s, fill, 12, None, op=op) + circle(x + s / 2, y + s / 2, s * 0.17, fill, None)


def frame(title, body, tab=None, back=False):
    o = [f"<svg xmlns='http://www.w3.org/2000/svg' width='{W}' height='{H}' viewBox='0 0 {W} {H}'>",
         "<defs><linearGradient id='hero' x1='0' y1='0' x2='1' y2='1'>"
         f"<stop offset='0' stop-color='{EM}' stop-opacity='.34'/><stop offset='.5' stop-color='#0A3828'/>"
         f"<stop offset='1' stop-color='{BG}'/></linearGradient></defs>",
         rect(0, 0, W, H, BG, 0, None),
         text(24, 22, "9:41", 12, TXT, 600), text(W - 24, 22, "100%", 11, TXT2, 500, "end"),
         # top bar
         ]
    if back:
        o += [text(20, 66, "‹", 30, EM, 400), text(48, 62, title, 18, TXT, 700)]
    else:
        o += [circle(36, 58, 16, EM), text(36, 63, "R", 15, TXT, 800, "middle"),
              text(62, 55, "Ruhani", 17, TXT, 800), text(62, 71, "Assalamu Alaikum", 10, TXT2),
              circle(W - 74, 58, 14, S2, OUT), circle(W - 36, 58, 14, S2, OUT)]
    o.append(body)
    # bottom nav (Home -> Prayer -> Quran -> More with capsule pill on active tab)
    o.append(rect(0, H - 74, W, 74, S1, 0, OUT))
    for i, (k, lab) in enumerate([("home", "Home"), ("prayer", "Prayer"), ("quran", "Quran"), ("more", "More")]):
        cx = 49 + i * 97
        act = tab == k
        if act:
            # Pill capsule enclosing both icon and label
            o.append(rect(cx - 34, H - 66, 68, 48, EM, 24, None, op=0.24))
        o.append(circle(cx, H - 50, 8, EM if act else TXT2))
        o.append(text(cx, H - 26, lab, 11, EM if act else TXT2, 700 if act else 500, "middle"))
    o.append("</svg>")
    return "\n".join(o)


def feature_card(x, y, w, title, sub, badge=None):
    s = [card(x, y, w, 92, True), icon_box(x + 12, y + 12, 36), text(x + 12, y + 66, title, 12, TXT, 700),
         text(x + 12, y + 82, sub, 9.5, TXT2)]
    if badge:
        s.append(pill(x + w - 12 - (len(badge) * 6 + 14), y + 14, len(badge) * 6 + 14, badge, S3, MINT, 18))
    return "".join(s)


# ---------------- screens ----------------
def home():
    b = [rect(16, 92, 358, 128, "url(#hero)", 22, EM, 1.2),
         pill(30, 104, 78, "Active Waqt", EM), text(30, 148, "Asr", 28, TXT, 800),
         text(30, 170, "Ends in 01:24:08", 12, TXT2), text(30, 196, "4:12 PM  ·  Dhaka", 12, MINT, 600),
         text(344, 150, "Next: Maghrib", 11, TXT2, 500, "end"), text(344, 170, "5:48 PM", 18, TXT, 800, "end")]
    for i, n in enumerate(["Fajr", "Dhuhr", "Asr", "Maghrib", "Isha"]):
        x = 16 + i * 72
        cur = n == "Asr"
        b += [rect(x, 232, 66, 52, EM if cur else S1, 14, OUT if not cur else None),
              text(x + 33, 252, n, 11, TXT, 700, "middle"), text(x + 33, 272, "4:12" if cur else "5:0" + str(i), 11,
                                                                    TXT if cur else TXT2, 600, "middle")]
    b += [text(16, 312, "Essential Services", 15, TXT, 700), pill(150, 298, 52, "Top 10", S2, TXT2),
          text(374, 312, "View All →", 11, EM, 700, "end")]
    items = [("Prayer Times", "5 Waqt & Forbidden"), ("Al-Quran", "Tafsir & Audio"), ("Tasbih", "Digital Dhikr"),
             ("Dua & Dhikr", "Morning, Evening"), ("Ramadan", "Sehri · Iftar"), ("Qibla", "Compass"),
             ("Zakat", "Nisab in BDT"), ("Calendar", "Hijri & Bangla"), ("99 Names", "Asma-ul-Husna"),
             ("Hadith", "Bukhari, Muslim")]
    for i, (t, s) in enumerate(items):
        b.append(feature_card(16 + (i % 2) * 182, 326 + (i // 2) * 100, 176, t, s))
    # more button + hadith (clipped by the scroll area, shown as continuation)
    b += [rect(16, 832 - 76 - 0, 358, 0, S1, 0, None)]
    return frame("", "".join(b), "home")


def home_scroll_tail():
    """Second SVG: the lower part of Home (More button, Hadith, Haramain)."""
    b = [rect(16, 92, 358, 66, S1, 16, EM, 1.2), icon_box(28, 105, 40), text(80, 124, "Explore More Services", 13, TXT, 700),
         text(80, 142, "Fatwa, Books, Hajj Guide, Baby Names & More", 9.5, TXT2), pill(300, 114, 60, "+6 More", S3, EM),
         card(16, 172, 358, 214), icon_box(30, 186, 26), text(64, 204, "Hadith of the Day", 12, MINT, 700),
         text(344, 204, "Bukhari 1", 10, TXT2, 400, "end"),
         text(30, 238, "“Actions are judged by intentions,", 14, TXT, 500),
         text(30, 258, "and every person will get what", 14, TXT, 500), text(30, 278, "they intended.”", 14, TXT, 500),
         text(30, 308, "Narrated by Umar ibn al-Khattab", 10, TXT2),
         f"<line x1='30' y1='328' x2='360' y2='328' stroke='{OUT}'/>",
         text(30, 356, "Narrated by Umar", 10, TXT2), text(344, 356, "Share Hadith ↗", 11, MINT, 700, "end"),
         card(16, 400, 358, 70), icon_box(28, 415, 40), text(80, 434, "Makkah & Madinah 24/7 Live", 13, TXT, 700),
         pill(80, 442, 38, "LIVE", RED, TXT, 16), text(126, 455, "Masjid al-Haram & an-Nabawi", 10, TXT2)]
    return frame("", "".join(b), "home")


def quran():
    b = [rect(16, 92, 358, 44, S1, 22, OUT), text(40, 119, "Search Surah or Ayah…", 13, TXT2),
         rect(16, 148, 358, 70, "url(#hero)", 18, EM, 1.2), text(30, 174, "Continue Reading", 11, MINT, 700),
         text(30, 198, "Al-Fatihah  ·  Ayah 1", 16, TXT, 800), text(344, 190, "▶", 20, EM, 700, "end")]
    for i, t in enumerate(["All", "Makki", "Madani", "Juz"]):
        b.append(pill(16 + i * 74, 232, 66, t, EM if i == 0 else S2, TXT if i == 0 else TXT2, 28))
    names = [("1", "Al-Fatihah", "The Opening · 7"), ("2", "Al-Baqarah", "The Cow · 286"),
             ("3", "Ali 'Imran", "Family of Imran · 200"), ("4", "An-Nisa", "The Women · 176"),
             ("5", "Al-Ma'idah", "The Table · 120"), ("6", "Al-An'am", "The Cattle · 165"),
             ("7", "Al-A'raf", "The Heights · 206"), ("8", "Al-Anfal", "Spoils of War · 75")]
    for i, (n, a, s) in enumerate(names):
        y = 276 + i * 68
        b += [card(16, y, 358, 60), rect(28, y + 14, 32, 32, S3, 10, None), text(44, y + 35, n, 12, MINT, 800, "middle"),
              text(74, y + 28, a, 14, TXT, 700), text(74, y + 46, s, 10, TXT2)]
    return frame("", "".join(b), "quran")


def prayer():
    b = [rect(16, 92, 358, 40, S1, 20, OUT), text(36, 117, "‹", 20, EM), text(195, 117, "Wed, 11 Nov 2026", 13, TXT, 700, "middle"),
         text(354, 117, "›", 20, EM, 400, "end"), rect(16, 142, 358, 38, S1, 19, EM),
         text(30, 166, "📍 Dhaka  ·  Karachi Method · Hanafi", 11, TXT2), text(350, 166, "Change", 11, EM, 700, "end")]
    rows = [("Fajr", "4:42 AM", False), ("Sunrise", "6:01 AM", False), ("Dhuhr", "11:48 AM", False),
            ("Asr", "4:12 PM", True), ("Maghrib", "5:48 PM", False), ("Isha", "7:06 PM", False)]
    for i, (n, t, cur) in enumerate(rows):
        y = 194 + i * 62
        b += [rect(16, y, 358, 54, EM if cur else S1, 16, None if cur else OUT), text(32, y + 32, n, 15, TXT, 700),
              text(300, y + 32, t, 14, TXT, 700, "end"), circle(344, y + 27, 9, S3 if not cur else TXT, None)]
    b += [rect(16, 574, 358, 100, "#2A0A0A", 18, RED), text(30, 600, "⚠ Prohibited (Makruh) Times", 13, RED, 800),
          text(30, 624, "Sunrise  5:56 – 6:11 AM", 11, TXT2), text(30, 642, "Zenith  11:42 – 11:45 AM", 11, TXT2),
          text(30, 660, "Sunset  5:33 – 5:48 PM", 11, TXT2)]
    return frame("", "".join(b), "prayer")


def more():
    b = [card(16, 92, 358, 72, True), circle(52, 128, 22, EM), text(52, 134, "M", 18, TXT, 800, "middle"),
         text(86, 122, "Muslim User", 15, TXT, 700), text(86, 142, "Profile & Cloud Backup", 11, TXT2),
         rect(16, 176, 358, 40, S1, 20, OUT), text(38, 201, "Search services…", 12, TXT2)]
    items = ["Digital Tasbih", "Prayer & Qaza Tracker", "Dua, Adhkar & Ruqyah", "Ramadan Dashboard", "Qibla Compass",
             "Zakat Calculator", "Tri-Calendar", "99 Names of Allah", "Hadith Collection", "Nearby Mosques"]
    for i, t in enumerate(items):
        y = 230 + i * 54
        b += [card(16, y, 358, 46), icon_box(24, y + 5, 36), text(72, y + 29, t, 13, TXT, 600),
              text(352, y + 30, "›", 18, EM, 400, "end")]
    return frame("", "".join(b), "more")


def tasbih():
    b = [pill(130, 92, 130, "Digital Tasbih • Local Save", S3, MINT, 22), text(195, 150, "سُبْحَانَ ٱللَّٰه", 26, TXT, 700, "middle"),
         text(195, 172, "SubhanAllah", 12, TXT2, 400, "middle"), circle(195, 300, 100, S1, EM, 3),
         circle(195, 300, 82, S2, OUT, 1), text(195, 322, "24", 64, TXT, 800, "middle"),
         text(195, 346, "/ 33", 14, RED, 700, "middle"), pill(150, 420, 90, "Today: 124", RED, TXT, 22)]
    for i, t in enumerate(["SubhanAllah", "Alhamdulillah", "Allahu Akbar"]):
        b.append(pill(20 + i * 120, 468, 112, t, EM if i == 0 else S2, TXT if i == 0 else TXT2, 28))
    b += [rect(20, 520, 168, 52, EM, 16, None), text(104, 552, "+1 Count", 14, TXT, 800, "middle"),
          rect(200, 520, 170, 52, RED, 16, None), text(285, 552, "Reset", 14, TXT, 800, "middle"),
          card(20, 590, 350, 60), text(36, 616, "Target", 12, TXT2), text(36, 636, "33   99   100   Custom", 13, TXT, 700),
          card(20, 662, 350, 60), text(36, 688, "Vibration", 12, TXT2), text(36, 708, "On", 13, TXT, 700),
          text(200, 688, "Sound", 12, TXT2), text(200, 708, "Off", 13, TXT, 700)]
    return frame("Digital Tasbih", "".join(b), None, True)


def dua():
    b = [rect(16, 92, 358, 44, S1, 22, EM, 1.2), text(40, 119, "Search by name or occasion…", 13, TXT2)]
    for i, t in enumerate(["All", "Morning", "Evening", "Salat", "Sleep", "Travel"]):
        w = 52 + len(t) * 4
        x = 16 + sum(52 + len(c) * 4 + 8 for c in ["All", "Morning", "Evening", "Salat", "Sleep", "Travel"][:i])
        b.append(pill(x, 148, w, t, EM if i == 0 else S2, TXT if i == 0 else TXT2, 26))
    for i, (t, a) in enumerate([("Morning Dhikr", "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ"),
                                ("Before Sleep", "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا"),
                                ("Entering Home", "بِسْمِ اللَّهِ وَلَجْنَا")]):
        y = 190 + i * 178
        b += [card(16, y, 358, 168), text(30, y + 26, t, 14, TXT, 700), text(344, y + 26, "♡", 16, RED, 400, "end"),
              text(344, y + 70, a, 15, TXT, 500, "end"),
              text(30, y + 100, "Translation in Bangla & English shown here…", 10.5, TXT2),
              rect(30, y + 122, 80, 32, EM, 12, None), text(70, y + 143, "+1 Count", 11, TXT, 700, "middle"),
              rect(120, y + 122, 60, 32, S2, 12, OUT), text(150, y + 143, "Reset", 11, TXT2, 700, "middle"),
              rect(190, y + 122, 60, 32, S2, 12, OUT), text(220, y + 143, "Speak", 11, TXT2, 700, "middle")]
    return frame("Dua & Dhikr", "".join(b), None, True)


def hadith():
    b = [rect(16, 92, 358, 44, S1, 22, OUT), text(40, 119, "Search Hadith…", 13, TXT2)]
    for i, t in enumerate(["Bukhari", "Muslim", "Riyadus"]):
        b.append(pill(16 + i * 92, 148, 84, t, EM if i == 0 else S2, TXT if i == 0 else TXT2, 26))
    for i in range(3):
        y = 190 + i * 170
        b += [card(16, y, 358, 160), pill(30, y + 14, 70, "Sahih", S3, MINT, 18), text(344, y + 28, "Bukhari " + str(i + 1), 10, TXT2, 400, "end"),
              text(30, y + 62, "“Hadith translation line one goes here", 13, TXT, 500),
              text(30, y + 82, "and continues on the next line…”", 13, TXT, 500),
              text(30, y + 112, "Narrated by Abu Hurairah", 10, TXT2),
              text(344, y + 146, "Share ↗", 11, MINT, 700, "end")]
    return frame("Hadith Collection", "".join(b), None, True)


def qibla():
    b = [circle(195, 300, 120, S1, EM, 2), circle(195, 300, 100, S2, OUT, 1)]
    for a, l in [(195, "N"), (315, "E"), (195, "S"), (75, "W")]:
        pass
    b += [text(195, 196, "N", 14, RED, 800, "middle"), text(309, 306, "E", 13, TXT2, 700, "middle"),
          text(195, 416, "S", 13, TXT2, 700, "middle"), text(81, 306, "W", 13, TXT2, 700, "middle"),
          "<polygon points='195,210 210,300 195,290 180,300' fill='#14A673'/>",
          "<polygon points='195,390 210,300 195,310 180,300' fill='#133024'/>", circle(195, 300, 8, TXT, EM, 2),
          text(195, 470, "277° West of North", 20, TXT, 800, "middle"), text(195, 494, "From Dhaka to Kaaba", 12, TXT2, 400, "middle"),
          card(20, 520, 170, 80), text(34, 546, "Distance", 11, TXT2), text(34, 576, "7,053 km", 18, MINT, 800),
          card(200, 520, 170, 80), text(214, 546, "Source", 11, TXT2), text(214, 576, "AlAdhan API", 16, MINT, 800)]
    return frame("Qibla Compass", "".join(b), None, True)


def zakat():
    b = [card(16, 92, 358, 76, True), text(30, 118, "Nisab (Silver)", 11, TXT2), text(30, 148, "৳ 98,420", 24, MINT, 800),
         text(344, 148, "2.5%", 18, GOLD, 800, "end")]
    for i, (t, v) in enumerate([("Cash & Savings", "৳ 0"), ("Gold (grams)", "0"), ("Silver (grams)", "0"),
                                ("Business Assets", "৳ 0"), ("Debts", "৳ 0")]):
        y = 182 + i * 70
        b += [text(20, y + 14, t, 11, TXT2), rect(16, y + 20, 358, 40, S1, 14, OUT), text(32, y + 46, v, 14, TXT, 600)]
    b += [rect(16, 544, 358, 50, EM, 16, None), text(195, 575, "Calculate Zakat", 14, TXT, 800, "middle"),
          card(16, 610, 358, 76), text(30, 636, "Zakat Payable", 11, TXT2), text(30, 668, "৳ 0", 24, TXT, 800)]
    return frame("Zakat Calculator", "".join(b), None, True)


def settings():
    b = []
    sec = [("Theme", "Dark   Light   System"), ("Language", "বাংলা   English"), ("Digits", "Bangla digits: Off"),
           ("District", "Dhaka"), ("Calculation Method", "Karachi (Hanafi)"), ("Azan Alerts", "On"),
           ("Notifications", "Waqt · Content · Personalized"), ("Privacy", "Export data · Delete account")]
    for i, (t, v) in enumerate(sec):
        y = 92 + i * 72
        b += [card(16, y, 358, 62), text(30, y + 26, t, 13, TXT, 700), text(30, y + 46, v, 11, TXT2),
              text(352, y + 38, "›", 18, EM, 400, "end")]
    return frame("Settings", "".join(b), None, True)


def profile():
    b = [circle(195, 150, 44, EM), text(195, 162, "M", 36, TXT, 800, "middle"),
         text(195, 218, "Muslim User", 18, TXT, 800, "middle"), text(195, 240, "Guest mode · Offline", 11, TXT2, 400, "middle"),
         rect(16, 270, 358, 52, TXT, 16, None), text(195, 302, "Sign in with Google (Cloud Sync)", 13, BG, 800, "middle"),
         card(16, 340, 358, 80), text(30, 366, "Tasbih Total", 11, TXT2), text(30, 396, "1,240", 22, MINT, 800),
         text(200, 366, "Qaza Prayers", 11, TXT2), text(200, 396, "12", 22, GOLD, 800),
         card(16, 434, 358, 56), text(30, 468, "Offline sync engine · Up to date", 12, TXT, 600),
         rect(16, 504, 358, 48, S1, 16, RED), text(195, 534, "Sign out", 13, RED, 800, "middle")]
    return frame("Profile", "".join(b), None, True)


SCREENS = {"01_home": home, "02_home_lower": home_scroll_tail, "03_quran": quran, "04_prayer": prayer, "05_more": more,
           "06_tasbih": tasbih, "07_dua_dhikr": dua, "08_hadith": hadith, "09_qibla": qibla, "10_zakat": zakat,
           "11_settings": settings, "12_profile": profile}

if __name__ == "__main__":
    out = os.path.join(os.path.dirname(os.path.abspath(__file__)), "svg")
    os.makedirs(out, exist_ok=True)
    for name, fn in SCREENS.items():
        with open(os.path.join(out, name + ".svg"), "w", encoding="utf-8") as f:
            f.write(fn())
    print("wrote", len(SCREENS), "svgs to", out)

