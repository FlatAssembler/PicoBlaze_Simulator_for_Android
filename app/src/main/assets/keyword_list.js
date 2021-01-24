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
const preprocessor = ["ADDRESS", "ORG", "VHDL", "EQU", "NAMEREG", "CONSTANT"];
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
