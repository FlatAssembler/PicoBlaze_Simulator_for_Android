let areWeHighlighting = false;
const mnemonics = [
  "ADD",
  "ADDCY",
  "ADDC",
  "AND",
  "CALL",
  "CALL@",
  "COMPARE",
  "COMP",
  "DISABLE",
  "ENABLE",
  "FETCH",
  "INPUT",
  "IN",
  "JUMP",
  "JUMP@",
  "LOAD",
  "OR",
  "OUTPUT",
  "OUT",
  "RETURN",
  "RET",
  "RETURNI",
  "RETI",
  "RL",
  "RR",
  "SL0",
  "SL1",
  "SLA",
  "SLX",
  "SR0",
  "SR1",
  "SRA",
  "SRX",
  "STORE",
  "SUB",
  "SUBCY",
  "SUBC",
  "TEST",
  "XOR",
  "INST",
  "LOAD&RETURN",
  "HWBUILD",
  "STAR",
  "OUTPUTK",
  "REGBANK",
  "TESTCY",
  "TESTC",
  "COMPARECY",
  "COMPCY",
];
const preprocessor = [
  "ADDRESS",
  "ORG",
  "VHDL",
  "EQU",
  "NAMEREG",
  "CONSTANT",
  "DISPLAY",
  "IF",
  "ELSE",
  "ENDIF",
  "WHILE",
  "ENDWHILE",
];

let machineCode = [];
for (let i = 0; i < 4096; i++) machineCode.push({ hex: "00000", line: 0 });

let breakpoints = [];

function formatAsAddress(n) {
  let ret = Math.round(n).toString(16);
  if (Math.round(n) >= 4096 || Math.round(n) < 0) {
    alert(
      "Some part of the compiler tried to format the number " +
        n +
        " as an address, which makes no sense."
    );
    return "fff";
  }
  while (ret.length < 3) ret = "0" + ret;
  return ret;
}

function formatAsByte(n) {
  n = Math.round(n);
  if (n < 0 || n > 255) {
    alert(
      "Some part of the assembler tried to format the number " +
        n +
        " as a byte, which makes no sense."
    );
    return "ff";
  }
  let ret = n.toString(16);
  while (ret.length < 2) ret = "0" + ret;
  return ret;
}

function formatAsInstruction(n) {
  n = Math.round(n);
  if (n < 0 || n >= 1 << 18) {
    alert(
      "Some part of the assembler tried to format the number " +
        n +
        " as a byte, which makes no sense."
    );
    return "ff";
  }
  let ret = n.toString(16);
  while (ret.length < 5) ret = "0" + ret;
  return ret;
}

function formatAs4bits(n) {
  n = Math.round(n);
  if (n < 0 || n >= 1 << 4) {
    alert(
      "Some part of the assembler tried to format the number " +
        n +
        " as a 4 bits, which makes no sense."
    );
    return "f";
  }
  let ret = n.toString(16);
  while (ret.length < 1) ret = "0" + ret;
  return ret;
}

function isDirective(str) {
  if (typeof str !== "string") {
    alert(
      'Internal compiler error: The first argument of the "isDirective" function is not a string!'
    );
    return false;
  }
  for (const directive of preprocessor)
    if (RegExp("^" + directive + "$", "i").test(str)) return true;
  if (/:$/.test(str)) return true;
  return false;
}
