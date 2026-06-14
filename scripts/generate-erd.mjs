import fs from "node:fs";
import path from "node:path";

const WIDTH = 1920;
const HEIGHT = 1320;

const colors = {
  canvas: "#FFFFFF",
  ink: "#161616",
  muted: "#525252",
  grid: "#D6D6D6",
  header: "#A8A8A8",
  keyCell: "#F4F4F4",
  blue: "#0043CE",
  blueDark: "#002D9C",
  blueLight: "#EDF5FF",
};

const tables = [
  {
    id: "word",
    title: "WORD",
    x: 60,
    y: 70,
    width: 400,
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
    id: "user",
    title: "APP_USER",
    x: 760,
    y: 70,
    width: 400,
    rows: [
      ["PK", "ID", "BIGINT"],
      ["UK", "USERNAME", "VARCHAR(50)"],
      ["", "PASSWORD_HASH", "VARCHAR(128)"],
      ["", "CREATED_AT", "TIMESTAMP"],
      ["", "UPDATED_AT", "TIMESTAMP"],
    ],
  },
  {
    id: "grammar",
    title: "GRAMMAR_NOTE",
    x: 1460,
    y: 70,
    width: 400,
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
    id: "status",
    title: "USER_WORD_STATUS",
    x: 60,
    y: 650,
    width: 400,
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
    id: "post",
    title: "COMMUNITY_POST",
    x: 760,
    y: 820,
    width: 400,
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
    id: "comment",
    title: "COMMUNITY_COMMENT",
    x: 1460,
    y: 690,
    width: 400,
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

const HEADER_HEIGHT = 42;
const ROW_HEIGHT = 38;
const KEY_WIDTH = 56;
const NAME_WIDTH = 186;

function escapeXml(value) {
  return value
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;");
}

function tableHeight(table) {
  return HEADER_HEIGHT + table.rows.length * ROW_HEIGHT;
}

function tableSvg(table) {
  const height = tableHeight(table);
  const rowElements = table.rows
    .map(([key, name, type], index) => {
      const y = table.y + HEADER_HEIGHT + index * ROW_HEIGHT;
      const keyFill = key ? colors.keyCell : colors.canvas;
      return `
        <rect x="${table.x}" y="${y}" width="${KEY_WIDTH}" height="${ROW_HEIGHT}" fill="${keyFill}"/>
        <line x1="${table.x + KEY_WIDTH}" y1="${y}" x2="${table.x + KEY_WIDTH}" y2="${y + ROW_HEIGHT}" class="cell-line"/>
        <line x1="${table.x + KEY_WIDTH + NAME_WIDTH}" y1="${y}" x2="${table.x + KEY_WIDTH + NAME_WIDTH}" y2="${y + ROW_HEIGHT}" class="cell-line"/>
        <line x1="${table.x}" y1="${y + ROW_HEIGHT}" x2="${table.x + table.width}" y2="${y + ROW_HEIGHT}" class="cell-line"/>
        <text x="${table.x + KEY_WIDTH / 2}" y="${y + 24}" text-anchor="middle" class="key">${escapeXml(key)}</text>
        <text x="${table.x + KEY_WIDTH + 16}" y="${y + 24}" class="column">${escapeXml(name)}</text>
        <text x="${table.x + KEY_WIDTH + NAME_WIDTH + 16}" y="${y + 24}" class="type">${escapeXml(type)}</text>
      `;
    })
    .join("");

  return `
    <g id="${table.id}" filter="url(#tableShadow)">
      <rect x="${table.x}" y="${table.y}" width="${table.width}" height="${height}" rx="2" fill="${colors.canvas}" class="table-border"/>
      <rect x="${table.x}" y="${table.y}" width="${table.width}" height="${HEADER_HEIGHT}" rx="2" fill="${colors.header}"/>
      <line x1="${table.x}" y1="${table.y + HEADER_HEIGHT}" x2="${table.x + table.width}" y2="${table.y + HEADER_HEIGHT}" class="table-border"/>
      <text x="${table.x + table.width / 2}" y="${table.y + 27}" text-anchor="middle" class="table-title">${table.title}</text>
      ${rowElements}
    </g>
  `;
}

function diamond(cx, cy, label, width = 132, height = 74) {
  const points = [
    `${cx},${cy - height / 2}`,
    `${cx + width / 2},${cy}`,
    `${cx},${cy + height / 2}`,
    `${cx - width / 2},${cy}`,
  ].join(" ");
  return `
    <g>
      <polygon points="${points}" fill="${colors.blue}" stroke="${colors.blueDark}" stroke-width="3"/>
      <text x="${cx}" y="${cy + 5}" text-anchor="middle" class="relationship-label">${label}</text>
    </g>
  `;
}

function line(x1, y1, x2, y2) {
  return `<line x1="${x1}" y1="${y1}" x2="${x2}" y2="${y2}" class="relation-line"/>`;
}

function polyline(points) {
  return `<polyline points="${points.map(([x, y]) => `${x},${y}`).join(" ")}" class="relation-line" fill="none"/>`;
}

function oneMarker(x, y, orientation = "horizontal") {
  if (orientation === "horizontal") {
    return `
      <line x1="${x - 8}" y1="${y - 12}" x2="${x - 8}" y2="${y + 12}" class="cardinality"/>
      <line x1="${x + 2}" y1="${y - 12}" x2="${x + 2}" y2="${y + 12}" class="cardinality"/>
    `;
  }
  return `
    <line x1="${x - 12}" y1="${y - 8}" x2="${x + 12}" y2="${y - 8}" class="cardinality"/>
    <line x1="${x - 12}" y1="${y + 2}" x2="${x + 12}" y2="${y + 2}" class="cardinality"/>
  `;
}

function manyMarker(x, y, orientation = "horizontal", optional = true) {
  if (orientation === "horizontal") {
    return `
      ${optional ? `<circle cx="${x - 13}" cy="${y}" r="7" fill="${colors.canvas}" class="cardinality"/>` : ""}
      <line x1="${x}" y1="${y}" x2="${x + 18}" y2="${y - 13}" class="cardinality"/>
      <line x1="${x}" y1="${y}" x2="${x + 18}" y2="${y}" class="cardinality"/>
      <line x1="${x}" y1="${y}" x2="${x + 18}" y2="${y + 13}" class="cardinality"/>
    `;
  }
  return `
    ${optional ? `<circle cx="${x}" cy="${y - 13}" r="7" fill="${colors.canvas}" class="cardinality"/>` : ""}
    <line x1="${x}" y1="${y}" x2="${x - 13}" y2="${y + 18}" class="cardinality"/>
    <line x1="${x}" y1="${y}" x2="${x}" y2="${y + 18}" class="cardinality"/>
    <line x1="${x}" y1="${y}" x2="${x + 13}" y2="${y + 18}" class="cardinality"/>
  `;
}

const wordBottom = 70 + tableHeight(tables[0]);
const userBottom = 70 + tableHeight(tables[1]);
const statusTop = 650;
const postTop = 820;
const commentTop = 690;

const relationships = `
  <!-- WORD 1 to USER_WORD_STATUS 0..N -->
  ${line(260, wordBottom, 260, 534)}
  ${diamond(260, 570, "RECORDED IN", 142, 72)}
  ${line(260, 606, 260, statusTop)}
  ${oneMarker(260, wordBottom + 18, "vertical")}
  ${manyMarker(260, statusTop - 18, "vertical", true)}

  <!-- APP_USER 1 to USER_WORD_STATUS 0..N -->
  ${line(760, 278, 624, 556)}
  ${diamond(570, 584, "TRACKS", 118, 68)}
  ${line(530, 614, 450, 790)}
  ${oneMarker(744, 302, "horizontal")}
  ${manyMarker(466, 756, "horizontal", true)}

  <!-- APP_USER 1 to COMMUNITY_POST 0..N -->
  ${line(960, userBottom, 960, 590)}
  ${diamond(960, 640, "WRITES", 118, 68)}
  ${line(960, 674, 960, postTop)}
  ${oneMarker(960, userBottom + 20, "vertical")}
  ${manyMarker(960, postTop - 18, "vertical", true)}

  <!-- APP_USER 1 to COMMUNITY_COMMENT 0..N -->
  ${line(1160, 262, 1311, 478)}
  ${diamond(1370, 478, "WRITES", 118, 68)}
  ${line(1429, 478, 1510, commentTop)}
  ${oneMarker(1174, 280, "horizontal")}
  ${manyMarker(1498, commentTop - 20, "horizontal", true)}

  <!-- COMMUNITY_POST 1 to COMMUNITY_COMMENT 0..N -->
  ${line(1160, 950, 1268, 950)}
  ${diamond(1320, 950, "CONTAINS", 132, 72)}
  ${line(1386, 950, 1460, 950)}
  ${oneMarker(1178, 950, "horizontal")}
  ${manyMarker(1442, 950, "horizontal", true)}

  <!-- COMMUNITY_COMMENT optional parent to many replies -->
  <path d="M 1860 840 C 1910 840, 1910 1060, 1860 1060" class="relation-line" fill="none"/>
  ${manyMarker(1844, 840, "horizontal", true)}
  ${manyMarker(1844, 1060, "horizontal", true)}
  <text x="1880" y="956" text-anchor="middle" class="loop-label" transform="rotate(90 1880 956)">PARENT / REPLIES</text>
`;

const svg = `<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" width="${WIDTH}" height="${HEIGHT}" viewBox="0 0 ${WIDTH} ${HEIGHT}">
  <defs>
    <filter id="tableShadow" x="-10%" y="-10%" width="120%" height="120%">
      <feDropShadow dx="0" dy="2" stdDeviation="2.5" flood-color="#000000" flood-opacity="0.10"/>
    </filter>
    <style>
      text {
        font-family: Inter, "Segoe UI", Arial, sans-serif;
        fill: ${colors.ink};
      }
      .table-border { stroke: ${colors.ink}; stroke-width: 2.5; }
      .cell-line { stroke: ${colors.ink}; stroke-width: 1.5; }
      .table-title { font-size: 17px; font-weight: 700; letter-spacing: 0.8px; }
      .key { font-size: 13px; font-weight: 700; }
      .column { font-size: 13px; font-weight: 650; letter-spacing: 0.15px; }
      .type { font-size: 12px; fill: ${colors.muted}; }
      .relation-line { stroke: ${colors.blue}; stroke-width: 2.4; stroke-linecap: round; stroke-linejoin: round; }
      .cardinality { stroke: ${colors.blueDark}; stroke-width: 2.2; stroke-linecap: round; }
      .relationship-label { fill: #FFFFFF; font-size: 12px; font-weight: 700; letter-spacing: 0.5px; }
      .loop-label { fill: ${colors.blueDark}; font-size: 11px; font-weight: 700; letter-spacing: 0.8px; }
    </style>
  </defs>

  <rect width="${WIDTH}" height="${HEIGHT}" fill="${colors.canvas}"/>
  ${relationships}
  ${tables.map(tableSvg).join("")}
</svg>`;

const outputDir = path.resolve("docs/images");
fs.mkdirSync(outputDir, { recursive: true });

const svgPath = path.join(outputDir, "jlptcloud-erd.svg");

fs.writeFileSync(svgPath, svg, "utf8");

console.log(`Generated ${svgPath}`);
