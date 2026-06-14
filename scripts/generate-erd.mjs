import fs from "node:fs";
import path from "node:path";

const W = 2000;
const H = 1320;

const palette = {
  canvas: "#F5F5F7",
  card: "#FFFFFF",
  header: "#E8E8ED",
  key: "#F5F5F7",
  ink: "#1D1D1F",
  secondary: "#6E6E73",
  border: "#C7C7CC",
  rule: "#D2D2D7",
  blue: "#007AFF",
  blueDeep: "#0055B3",
  blueSoft: "#EAF3FF",
};

const CARD_W = 430;
const HEADER_H = 48;
const ROW_H = 37;
const KEY_W = 58;
const NAME_W = 206;

const tables = [
  {
    title: "WORD", x: 55, y: 60,
    rows: [
      ["PK", "ID", "BIGINT"],
      ["", "JAPANESE", "VARCHAR(100)"],
      ["", "READING", "VARCHAR(100)"],
      ["", "MEANING", "VARCHAR(150)"],
      ["", "PART_OF_SPEECH", "VARCHAR(50)"],
      ["", "EXAMPLE_SENTENCE", "VARCHAR(500)"],
      ["", "JLPT_LEVEL", "VARCHAR(10)"],
      ["", "STUDY_STATUS", "VARCHAR(20)"],
      ["", "CREATED_AT", "TIMESTAMP"],
      ["", "UPDATED_AT", "TIMESTAMP"],
    ],
  },
  {
    title: "APP_USER", x: 785, y: 60,
    rows: [
      ["PK", "ID", "BIGINT"],
      ["UK", "USERNAME", "VARCHAR(50)"],
      ["", "PASSWORD_HASH", "VARCHAR(128)"],
      ["", "CREATED_AT", "TIMESTAMP"],
      ["", "UPDATED_AT", "TIMESTAMP"],
    ],
  },
  {
    title: "GRAMMAR_NOTE", x: 1515, y: 60,
    rows: [
      ["PK", "ID", "BIGINT"],
      ["", "TITLE", "VARCHAR(120)"],
      ["", "PATTERN_EXPRESSION", "VARCHAR(120)"],
      ["", "MEANING", "VARCHAR(200)"],
      ["", "EXPLANATION", "VARCHAR(1000)"],
      ["", "EXAMPLE_SENTENCE", "VARCHAR(500)"],
      ["", "JLPT_LEVEL", "VARCHAR(10)"],
      ["", "STUDY_STATUS", "VARCHAR(20)"],
      ["", "CREATED_AT", "TIMESTAMP"],
      ["", "UPDATED_AT", "TIMESTAMP"],
    ],
  },
  {
    title: "USER_WORD_STATUS", x: 55, y: 680,
    rows: [
      ["PK", "ID", "BIGINT"],
      ["FK", "USER_ID", "BIGINT"],
      ["FK", "WORD_ID", "BIGINT"],
      ["", "STUDY_STATUS", "VARCHAR(20)"],
      ["", "STUDIED", "BOOLEAN"],
      ["", "MEMORY_STAGE", "INTEGER"],
      ["", "MEMORY_SCORE", "DOUBLE PRECISION"],
      ["", "CORRECT_STREAK", "INTEGER"],
      ["", "CORRECT_COUNT", "INTEGER"],
      ["", "WRONG_COUNT", "INTEGER"],
      ["", "REVIEW_COUNT", "INTEGER"],
      ["", "NEXT_REVIEW_AT", "TIMESTAMP"],
      ["", "LAST_REVIEWED_AT", "TIMESTAMP"],
      ["", "CREATED_AT", "TIMESTAMP"],
      ["", "UPDATED_AT", "TIMESTAMP"],
    ],
  },
  {
    title: "COMMUNITY_POST", x: 785, y: 880,
    rows: [
      ["PK", "ID", "BIGINT"],
      ["FK", "USER_ID", "BIGINT"],
      ["", "AUTHOR_NAME", "VARCHAR(80)"],
      ["", "TITLE", "VARCHAR(120)"],
      ["", "CONTENT", "VARCHAR(3000)"],
      ["", "CREATED_AT", "TIMESTAMP"],
      ["", "UPDATED_AT", "TIMESTAMP"],
    ],
  },
  {
    title: "COMMUNITY_COMMENT", x: 1515, y: 760,
    rows: [
      ["PK", "ID", "BIGINT"],
      ["FK", "POST_ID", "BIGINT"],
      ["FK", "PARENT_ID", "BIGINT"],
      ["FK", "USER_ID", "BIGINT"],
      ["", "AUTHOR_NAME", "VARCHAR(80)"],
      ["", "CONTENT", "VARCHAR(1200)"],
      ["", "CREATED_AT", "TIMESTAMP"],
      ["", "UPDATED_AT", "TIMESTAMP"],
    ],
  },
];

const esc = (text) => text.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
const cardHeight = (table) => HEADER_H + table.rows.length * ROW_H;

function table(table) {
  const height = cardHeight(table);
  const rows = table.rows.map(([key, name, type], i) => {
    const y = table.y + HEADER_H + i * ROW_H;
    return `
      <rect x="${table.x}" y="${y}" width="${KEY_W}" height="${ROW_H}" fill="${key ? palette.key : palette.card}"/>
      <line x1="${table.x + KEY_W}" y1="${y}" x2="${table.x + KEY_W}" y2="${y + ROW_H}" class="rule"/>
      <line x1="${table.x + KEY_W + NAME_W}" y1="${y}" x2="${table.x + KEY_W + NAME_W}" y2="${y + ROW_H}" class="rule"/>
      ${i < table.rows.length - 1 ? `<line x1="${table.x}" y1="${y + ROW_H}" x2="${table.x + CARD_W}" y2="${y + ROW_H}" class="rule"/>` : ""}
      <text x="${table.x + KEY_W / 2}" y="${y + 24}" text-anchor="middle" class="key-text">${esc(key)}</text>
      <text x="${table.x + KEY_W + 16}" y="${y + 24}" class="name-text">${esc(name)}</text>
      <text x="${table.x + KEY_W + NAME_W + 16}" y="${y + 24}" class="type-text">${esc(type)}</text>
    `;
  }).join("");

  return `
    <g filter="url(#cardShadow)">
      <rect x="${table.x}" y="${table.y}" width="${CARD_W}" height="${height}" rx="12" fill="${palette.card}" stroke="${palette.border}" stroke-width="1.4"/>
      <path d="M ${table.x + 12} ${table.y} H ${table.x + CARD_W - 12} Q ${table.x + CARD_W} ${table.y} ${table.x + CARD_W} ${table.y + 12} V ${table.y + HEADER_H} H ${table.x} V ${table.y + 12} Q ${table.x} ${table.y} ${table.x + 12} ${table.y}" fill="${palette.header}"/>
      <line x1="${table.x}" y1="${table.y + HEADER_H}" x2="${table.x + CARD_W}" y2="${table.y + HEADER_H}" stroke="${palette.border}" stroke-width="1.4"/>
      <text x="${table.x + CARD_W / 2}" y="${table.y + 31}" text-anchor="middle" class="title-text">${table.title}</text>
      ${rows}
    </g>
  `;
}

function relationPath(d) {
  return `<path d="${d}" class="relation-line"/>`;
}

function diamond(cx, cy, label, width = 150) {
  const half = width / 2;
  return `
    <g>
      <polygon points="${cx},${cy - 34} ${cx + half},${cy} ${cx},${cy + 34} ${cx - half},${cy}"
        fill="${palette.blue}" stroke="${palette.blueDeep}" stroke-width="2"/>
      <text x="${cx}" y="${cy + 4}" text-anchor="middle" class="relation-text">${label}</text>
    </g>
  `;
}

function cardinality(x, y, label) {
  const width = label === "1" ? 32 : 58;
  return `
    <g>
      <rect x="${x - width / 2}" y="${y - 15}" width="${width}" height="30" rx="15"
        fill="${palette.blueSoft}" stroke="${palette.blue}" stroke-width="1.5"/>
      <text x="${x}" y="${y + 5}" text-anchor="middle" class="cardinality-text">${label}</text>
    </g>
  `;
}

const wordBottom = tables[0].y + cardHeight(tables[0]);
const userBottom = tables[1].y + cardHeight(tables[1]);
const statusTop = tables[3].y;
const postTop = tables[4].y;
const commentTop = tables[5].y;

const relations = `
  <!-- WORD -> USER_WORD_STATUS -->
  ${relationPath(`M 270 ${wordBottom} V 546`)}
  ${diamond(270, 580, "RECORDED IN", 156)}
  ${relationPath(`M 270 614 V ${statusTop}`)}
  ${cardinality(270, wordBottom + 24, "1")}
  ${cardinality(270, statusTop - 24, "0..N")}

  <!-- APP_USER -> USER_WORD_STATUS -->
  ${relationPath("M 785 220 L 700 400 L 605 534")}
  ${diamond(605, 568, "TRACKS", 126)}
  ${relationPath("M 605 602 L 485 790")}
  ${cardinality(770, 245, "1")}
  ${cardinality(530, 720, "0..N")}

  <!-- APP_USER -> GRAMMAR_NOTE -->
  ${relationPath("M 1215 170 H 1280")}
  ${diamond(1365, 170, "USER GRAMMAR", 172)}
  ${relationPath("M 1451 170 H 1515")}
  ${cardinality(1244, 170, "1")}
  ${cardinality(1482, 170, "0..N")}

  <!-- APP_USER -> COMMUNITY_POST -->
  ${relationPath(`M 1000 ${userBottom} V 586`)}
  ${diamond(1000, 620, "WRITES", 128)}
  ${relationPath(`M 1000 654 V ${postTop}`)}
  ${cardinality(1000, userBottom + 28, "1")}
  ${cardinality(1000, postTop - 26, "0..N")}

  <!-- APP_USER -> COMMUNITY_COMMENT -->
  ${relationPath("M 1215 258 L 1305 390 L 1390 466")}
  ${diamond(1390, 500, "WRITES", 128)}
  ${relationPath(`M 1390 534 L 1515 ${commentTop}`)}
  ${cardinality(1232, 284, "1")}
  ${cardinality(1502, commentTop - 28, "0..N")}

  <!-- COMMUNITY_POST -> COMMUNITY_COMMENT -->
  ${relationPath("M 1215 1008 H 1291")}
  ${diamond(1365, 1008, "CONTAINS", 148)}
  ${relationPath("M 1439 1008 H 1515")}
  ${cardinality(1248, 1008, "1")}
  ${cardinality(1482, 1008, "0..N")}

  <!-- COMMENT self-reference -->
  ${relationPath("M 1945 856 C 1980 856 1980 1050 1945 1050")}
  ${cardinality(1962, 856, "0..1")}
  ${cardinality(1962, 1050, "0..N")}
  <text x="1980" y="953" text-anchor="middle" class="loop-text" transform="rotate(90 1980 953)">PARENT / REPLIES</text>
`;

const svg = `<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" width="${W}" height="${H}" viewBox="0 0 ${W} ${H}">
  <defs>
    <filter id="cardShadow" x="-10%" y="-10%" width="120%" height="125%">
      <feDropShadow dx="0" dy="5" stdDeviation="10" flood-color="#000000" flood-opacity="0.08"/>
    </filter>
    <style>
      text { font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Segoe UI", Arial, sans-serif; fill: ${palette.ink}; }
      .rule { stroke: ${palette.rule}; stroke-width: 1; }
      .title-text { font-size: 17px; font-weight: 700; letter-spacing: 0.8px; }
      .key-text { font-size: 12px; font-weight: 700; }
      .name-text { font-size: 12px; font-weight: 650; letter-spacing: 0.15px; }
      .type-text { font-size: 11px; font-weight: 450; fill: ${palette.secondary}; }
      .relation-line { fill: none; stroke: ${palette.blue}; stroke-width: 2.5; stroke-linecap: round; stroke-linejoin: round; }
      .relation-text { fill: #FFFFFF; font-size: 11px; font-weight: 700; letter-spacing: 0.35px; }
      .cardinality-text { fill: ${palette.blueDeep}; font-size: 11px; font-weight: 700; }
      .loop-text { fill: ${palette.blueDeep}; font-size: 11px; font-weight: 700; letter-spacing: 0.8px; }
    </style>
  </defs>
  <rect width="${W}" height="${H}" fill="${palette.canvas}"/>
  ${relations}
  ${tables.map(table).join("")}
</svg>`;

const outputDir = path.resolve("docs/images");
fs.mkdirSync(outputDir, { recursive: true });
fs.writeFileSync(path.join(outputDir, "jlptcloud-erd.svg"), svg, "utf8");
console.log(`Generated ${path.join(outputDir, "jlptcloud-erd.svg")}`);
