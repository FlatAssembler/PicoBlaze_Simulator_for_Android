package hr.ferit.teo_samarzija.picoblaze_simulator;

import android.util.Log;
import android.webkit.WebView;

import java.lang.reflect.Array;
import java.util.Stack;
import java.util.Timer;

public class Simulator {
    public String terminalOutput;
    public String terminalInput;
    public byte[] output;
    public byte[] memory;
    public byte[][] registers;
    public boolean regbank;
    public int PC;
    public int[] flagZ;
    public int[] flagC;
    public int flagIE;
    public Stack<Integer> callStack;
    public int currentlyReadCharacterInUART;
    Timer myTimer;

    public WebView referenceToTheWebViewInSimulation;
    private Simulator() {
        terminalOutput = AssembledProgram.getInstance().terminalOutputDuringAssembly;
        output = new byte[256];
        terminalInput = "";
        memory = new byte[256];
        registers = new byte[2][];
        registers[0] = new byte[16];
        registers[1] = new byte[16];
        regbank = false;
        PC = 0;
        flagZ = new int[]{0, 0};
        flagC = new int[]{0, 0};
        flagIE = 0;
        callStack = new Stack<>();
        currentlyReadCharacterInUART = 0;
        myTimer = new Timer();
    }

    public void simulateOneInstruction() {
        try {
            // If you are at the end of a program, and there is no "return" there, jump to the
            // beginning of the program. I think that's how PicoBlaze behaves.
            PC = PC % 4096;

            int regbankIndex = regbank ? 1 : 0;
            AssembledProgram program = AssembledProgram.getInstance();
            int currentDirective = program.instructions[PC];
            // Format the instruction as a 5-digit hex string for character-based parsing,
            // matching the JavaScript machineCode[PC].hex format.
            String hex = String.format("%05x", currentDirective);

            int port, firstRegister, secondRegister, firstValue, secondValue, result, value,
                    registerIndex, registerValue;

            // "bennyboy" from "atheistforums.org" thinks my program can be speeded up by using
            // a switch-case instead of the large if-else (that a switch-case would compile into
            // a more efficient assembly code), so it would be interesting to investigate whether
            // that's true: https://atheistforums.org/thread-61911-post-2112817.html#pid2112817
            switch (currentDirective & 0xff000) {
                case 0x00000:
                    // LOAD register, register
                    registers[regbankIndex][Integer.parseInt(String.valueOf(hex.charAt(2)), 16)] =
                            registers[regbankIndex][Integer.parseInt(String.valueOf(hex.charAt(3)), 16)];
                    PC++;
                    break;
                case 0x01000:
                    // LOAD register, constant
                    registers[regbankIndex][Integer.parseInt(String.valueOf(hex.charAt(2)), 16)] =
                            (byte) Integer.parseInt(hex.substring(3), 16);
                    PC++;
                    break;
                case 0x17000:
                    // STAR register, constant ;Storing a constant into an inactive register
                    registers[regbank ? 0 : 1][Integer.parseInt(String.valueOf(hex.charAt(2)), 16)] =
                            (byte) Integer.parseInt(hex.substring(3), 16);
                    PC++;
                    break;
                case 0x16000:
                    // STAR register, register ;Copying from an active register into an inactive one.
                    registers[regbank ? 0 : 1][Integer.parseInt(String.valueOf(hex.charAt(2)), 16)] =
                            registers[regbankIndex][Integer.parseInt(String.valueOf(hex.charAt(3)), 16)];
                    PC++;
                    break;
                case 0x2e000:
                    // STORE register, (register) ;Store the first register at the memory location
                    // where the second register points to.
                    firstRegister = Integer.parseInt(String.valueOf(hex.charAt(2)), 16);
                    secondRegister = Integer.parseInt(String.valueOf(hex.charAt(3)), 16);
                    memory[registers[regbankIndex][secondRegister] & 0xFF] =
                            registers[regbankIndex][firstRegister];
                    PC++;
                    break;
                case 0x2f000:
                    // STORE register, memory_address ;Copy a register onto a memory address.
                    firstRegister = Integer.parseInt(String.valueOf(hex.charAt(2)), 16);
                    memory[Integer.parseInt(hex.substring(3), 16)] =
                            registers[regbankIndex][firstRegister];
                    PC++;
                    break;
                case 0x0a000:
                    // FETCH register, (register) ;Dereference the pointer in the second register.
                    firstRegister = Integer.parseInt(String.valueOf(hex.charAt(2)), 16);
                    secondRegister = Integer.parseInt(String.valueOf(hex.charAt(3)), 16);
                    registers[regbankIndex][firstRegister] =
                            memory[registers[regbankIndex][secondRegister] & 0xFF];
                    PC++;
                    break;
                case 0x0b000:
                    // FETCH register, memory_address ;Copy the value at memory_address to the register.
                    firstRegister = Integer.parseInt(String.valueOf(hex.charAt(2)), 16);
                    registers[regbankIndex][firstRegister] =
                            memory[Integer.parseInt(hex.substring(3), 16)];
                    PC++;
                    break;
                case 0x08000: {
                    // INPUT register, (register) ;Read a byte from a port specified by a register.
                    firstRegister = Integer.parseInt(String.valueOf(hex.charAt(2)), 16);
                    secondRegister = Integer.parseInt(String.valueOf(hex.charAt(3)), 16);
                    port = registers[regbankIndex][secondRegister] & 0xFF;
                    if (port == 2 || port == 3) {
                        if (port == 3) {
                            // UART_RX_PORT
                            registers[regbankIndex][firstRegister] =
                                    currentlyReadCharacterInUART < terminalInput.length()
                                            ? (byte) (terminalInput.charAt(currentlyReadCharacterInUART) & 0xFF)
                                            : 0;
                            currentlyReadCharacterInUART++;
                        } else {
                            // UART_STATUS_PORT
                            registers[regbankIndex][firstRegister] =
                                    (byte) (currentlyReadCharacterInUART < terminalInput.length()
                                            ? 0b00001000 /*U_RX_D*/ : 0);
                        }
                    } else {
                        // No general input port mechanism in Android version; return 0
                        registers[regbankIndex][firstRegister] = 0;
                    }
                    PC++;
                    break;
                }
                case 0x09000: {
                    // INPUT register, port_number
                    firstRegister = Integer.parseInt(String.valueOf(hex.charAt(2)), 16);
                    port = Integer.parseInt(hex.substring(3), 16);
                    if (port == 2 || port == 3) {
                        if (port == 3) {
                            // UART_RX_PORT
                            registers[regbankIndex][firstRegister] =
                                    currentlyReadCharacterInUART < terminalInput.length()
                                            ? (byte) (terminalInput.charAt(currentlyReadCharacterInUART) & 0xFF)
                                            : 0;
                            currentlyReadCharacterInUART++;
                        } else {
                            // UART_STATUS_PORT
                            registers[regbankIndex][firstRegister] =
                                    (byte) (currentlyReadCharacterInUART < terminalInput.length()
                                            ? 0b00001000 /*U_RX_D*/ : 0);
                        }
                    } else {
                        // No general input port mechanism in Android version; return 0
                        registers[regbankIndex][firstRegister] = 0;
                    }
                    PC++;
                    break;
                }
                case 0x2c000: {
                    // OUTPUT register, (register) ;Output the result of the first register to
                    // the port specified by the second register.
                    firstRegister = Integer.parseInt(String.valueOf(hex.charAt(2)), 16);
                    secondRegister = Integer.parseInt(String.valueOf(hex.charAt(3)), 16);
                    port = registers[regbankIndex][secondRegister] & 0xFF;
                    value = registers[regbankIndex][firstRegister] & 0xFF;
                    if (port == 3 || port == 4) {
                        if (port == 3)
                            // UART_TX_PORT
                            terminalOutput += String.valueOf((char) value);
                        else
                            // UART_RESET_PORT
                            terminalOutput = "";
                    } else {
                        output[port] = (byte) value;
                    }
                    PC++;
                    break;
                }
                case 0x2d000: {
                    // OUTPUT register, port_number
                    firstRegister = Integer.parseInt(String.valueOf(hex.charAt(2)), 16);
                    port = Integer.parseInt(hex.substring(3), 16);
                    value = registers[regbankIndex][firstRegister] & 0xFF;
                    if (port == 3 || port == 4) {
                        if (port == 3)
                            // UART_TX_PORT
                            terminalOutput += String.valueOf((char) value);
                        else
                            // UART_RESET_PORT
                            terminalOutput = "";
                    } else {
                        output[port] = (byte) value;
                    }
                    PC++;
                    break;
                }
                case 0x2b000: {
                    // OUTPUTK constant, port_number
                    value = Integer.parseInt(hex.substring(2, 4), 16);
                    port = Integer.parseInt(String.valueOf(hex.charAt(4)), 16);
                    if (port == 3 || port == 4) {
                        if (port == 3)
                            // UART_TX_PORT
                            terminalOutput += String.valueOf((char) value);
                        else
                            // UART_RESET_PORT
                            terminalOutput = "";
                    } else {
                        output[port] = (byte) value;
                    }
                    PC++;
                    break;
                }
                case 0x37000:
                    // REGBANK A or REGBANK B
                    if ((currentDirective & 0x00ff0) != 0 || (currentDirective & 0xf) > 1) {
                        Log.e("PicoBlaze",
                                "Sorry about that, the simulator currently does not support the instruction \""
                                        + hex + "\" (" + currentDirective + " & " + 0xff000 + " = "
                                        + (currentDirective & 0xff000) + "), assembled from line #"
                                        + program.lineNumbers[PC] + ".");
                        myTimer.cancel();
                        break;
                    }
                    regbank = (currentDirective % 2 != 0);
                    PC++;
                    break;
                case 0x22000:
                    // JUMP label
                    PC = Integer.parseInt(hex.substring(2), 16);
                    break;
                case 0x10000: {
                    // ADD register, register
                    firstRegister = Integer.parseInt(String.valueOf(hex.charAt(2)), 16);
                    secondRegister = Integer.parseInt(String.valueOf(hex.charAt(3)), 16);
                    firstValue = registers[regbankIndex][firstRegister] & 0xFF;
                    secondValue = registers[regbankIndex][secondRegister] & 0xFF;
                    result = firstValue + secondValue;
                    flagZ[regbankIndex] = (result % 256 == 0) ? 1 : 0;
                    flagC[regbankIndex] = (result > 255) ? 1 : 0;
                    registers[regbankIndex][firstRegister] = (byte) (result & 0xFF);
                    PC++;
                    break;
                }
                case 0x11000: {
                    // ADD register, constant
                    firstRegister = Integer.parseInt(String.valueOf(hex.charAt(2)), 16);
                    firstValue = registers[regbankIndex][firstRegister] & 0xFF;
                    secondValue = Integer.parseInt(hex.substring(3), 16);
                    result = firstValue + secondValue;
                    flagZ[regbankIndex] = (result % 256 == 0) ? 1 : 0;
                    flagC[regbankIndex] = (result > 255) ? 1 : 0;
                    registers[regbankIndex][firstRegister] = (byte) (result & 0xFF);
                    PC++;
                    break;
                }
                case 0x12000: {
                    // ADDCY register, register
                    firstRegister = Integer.parseInt(String.valueOf(hex.charAt(2)), 16);
                    secondRegister = Integer.parseInt(String.valueOf(hex.charAt(3)), 16);
                    firstValue = registers[regbankIndex][firstRegister] & 0xFF;
                    secondValue = registers[regbankIndex][secondRegister] & 0xFF;
                    result = firstValue + secondValue + flagC[regbankIndex];
                    flagZ[regbankIndex] = (result % 256 == 0) ? 1 : 0;
                    flagC[regbankIndex] = (result > 255) ? 1 : 0;
                    registers[regbankIndex][firstRegister] = (byte) (result & 0xFF);
                    PC++;
                    break;
                }
                case 0x13000: {
                    // ADDCY register, constant
                    firstRegister = Integer.parseInt(String.valueOf(hex.charAt(2)), 16);
                    firstValue = registers[regbankIndex][firstRegister] & 0xFF;
                    secondValue = Integer.parseInt(hex.substring(3), 16);
                    result = firstValue + secondValue + flagC[regbankIndex];
                    flagZ[regbankIndex] = (result % 256 == 0) ? 1 : 0;
                    flagC[regbankIndex] = (result > 255) ? 1 : 0;
                    registers[regbankIndex][firstRegister] = (byte) (result & 0xFF);
                    PC++;
                    break;
                }
                case 0x18000: {
                    // SUB register, register
                    firstRegister = Integer.parseInt(String.valueOf(hex.charAt(2)), 16);
                    secondRegister = Integer.parseInt(String.valueOf(hex.charAt(3)), 16);
                    firstValue = registers[regbankIndex][firstRegister] & 0xFF;
                    secondValue = registers[regbankIndex][secondRegister] & 0xFF;
                    result = firstValue - secondValue;
                    flagZ[regbankIndex] = (result % 256 == 0) ? 1 : 0;
                    flagC[regbankIndex] = (result < 0) ? 1 : 0;
                    registers[regbankIndex][firstRegister] = (byte) (result & 0xFF);
                    PC++;
                    break;
                }
                case 0x19000: {
                    // SUB register, constant
                    firstRegister = Integer.parseInt(String.valueOf(hex.charAt(2)), 16);
                    firstValue = registers[regbankIndex][firstRegister] & 0xFF;
                    secondValue = Integer.parseInt(hex.substring(3), 16);
                    result = firstValue - secondValue;
                    flagZ[regbankIndex] = (result % 256 == 0) ? 1 : 0;
                    flagC[regbankIndex] = (result < 0) ? 1 : 0;
                    registers[regbankIndex][firstRegister] = (byte) (result & 0xFF);
                    PC++;
                    break;
                }
                case 0x1a000: {
                    // SUBCY register, register
                    firstRegister = Integer.parseInt(String.valueOf(hex.charAt(2)), 16);
                    secondRegister = Integer.parseInt(String.valueOf(hex.charAt(3)), 16);
                    firstValue = registers[regbankIndex][firstRegister] & 0xFF;
                    secondValue = registers[regbankIndex][secondRegister] & 0xFF;
                    result = firstValue - secondValue - flagC[regbankIndex];
                    flagZ[regbankIndex] = (result % 256 == 0) ? 1 : 0;
                    flagC[regbankIndex] = (result < 0) ? 1 : 0;
                    registers[regbankIndex][firstRegister] = (byte) (result & 0xFF);
                    PC++;
                    break;
                }
                case 0x1b000: {
                    // SUBCY register, constant
                    firstRegister = Integer.parseInt(String.valueOf(hex.charAt(2)), 16);
                    firstValue = registers[regbankIndex][firstRegister] & 0xFF;
                    secondValue = Integer.parseInt(hex.substring(3), 16);
                    result = firstValue - secondValue - flagC[regbankIndex];
                    flagZ[regbankIndex] = (result % 256 == 0) ? 1 : 0;
                    flagC[regbankIndex] = (result < 0) ? 1 : 0;
                    registers[regbankIndex][firstRegister] = (byte) (result & 0xFF);
                    PC++;
                    break;
                }
                case 0x02000: {
                    // AND register, register
                    firstRegister = Integer.parseInt(String.valueOf(hex.charAt(2)), 16);
                    secondRegister = Integer.parseInt(String.valueOf(hex.charAt(3)), 16);
                    firstValue = registers[regbankIndex][firstRegister] & 0xFF;
                    secondValue = registers[regbankIndex][secondRegister] & 0xFF;
                    result = firstValue & secondValue;
                    flagZ[regbankIndex] = (result % 256 == 0) ? 1 : 0;
                    flagC[regbankIndex] = (result % 256 == 255) ? 1 : 0;
                    registers[regbankIndex][firstRegister] = (byte) (result & 0xFF);
                    PC++;
                    break;
                }
                case 0x03000: {
                    // AND register, constant
                    firstRegister = Integer.parseInt(String.valueOf(hex.charAt(2)), 16);
                    firstValue = registers[regbankIndex][firstRegister] & 0xFF;
                    secondValue = Integer.parseInt(hex.substring(3), 16);
                    result = firstValue & secondValue;
                    flagZ[regbankIndex] = (result % 256 == 0) ? 1 : 0;
                    flagC[regbankIndex] = (result % 256 == 255) ? 1 : 0;
                    registers[regbankIndex][firstRegister] = (byte) (result & 0xFF);
                    PC++;
                    break;
                }
                case 0x04000: {
                    // OR register, register
                    firstRegister = Integer.parseInt(String.valueOf(hex.charAt(2)), 16);
                    secondRegister = Integer.parseInt(String.valueOf(hex.charAt(3)), 16);
                    firstValue = registers[regbankIndex][firstRegister] & 0xFF;
                    secondValue = registers[regbankIndex][secondRegister] & 0xFF;
                    result = firstValue | secondValue;
                    flagZ[regbankIndex] = (result % 256 == 0) ? 1 : 0;
                    flagC[regbankIndex] = (result % 256 == 255) ? 1 : 0;
                    registers[regbankIndex][firstRegister] = (byte) (result & 0xFF);
                    PC++;
                    break;
                }
                case 0x05000: {
                    // OR register, constant
                    firstRegister = Integer.parseInt(String.valueOf(hex.charAt(2)), 16);
                    firstValue = registers[regbankIndex][firstRegister] & 0xFF;
                    secondValue = Integer.parseInt(hex.substring(3), 16);
                    result = firstValue | secondValue;
                    flagZ[regbankIndex] = (result % 256 == 0) ? 1 : 0;
                    flagC[regbankIndex] = (result % 256 == 255) ? 1 : 0;
                    registers[regbankIndex][firstRegister] = (byte) (result & 0xFF);
                    PC++;
                    break;
                }
                case 0x06000: {
                    // XOR register, register
                    firstRegister = Integer.parseInt(String.valueOf(hex.charAt(2)), 16);
                    secondRegister = Integer.parseInt(String.valueOf(hex.charAt(3)), 16);
                    firstValue = registers[regbankIndex][firstRegister] & 0xFF;
                    secondValue = registers[regbankIndex][secondRegister] & 0xFF;
                    result = firstValue ^ secondValue;
                    flagZ[regbankIndex] = (result % 256 == 0) ? 1 : 0;
                    flagC[regbankIndex] = (result % 256 == 255) ? 1 : 0;
                    registers[regbankIndex][firstRegister] = (byte) (result & 0xFF);
                    PC++;
                    break;
                }
                case 0x07000: {
                    // XOR register, constant
                    firstRegister = Integer.parseInt(String.valueOf(hex.charAt(2)), 16);
                    firstValue = registers[regbankIndex][firstRegister] & 0xFF;
                    secondValue = Integer.parseInt(hex.substring(3), 16);
                    result = firstValue ^ secondValue;
                    flagZ[regbankIndex] = (result % 256 == 0) ? 1 : 0;
                    flagC[regbankIndex] = (result % 256 == 255) ? 1 : 0;
                    registers[regbankIndex][firstRegister] = (byte) (result & 0xFF);
                    PC++;
                    break;
                }
                case 0x0c000:
                case 0x0e000: {
                    // TEST register, register ;The same as "AND", but does not store the result
                    // (only the flags). I am not sure if there is a difference between "0c" and
                    // "0e", they appear to be the same.
                    firstRegister = Integer.parseInt(String.valueOf(hex.charAt(2)), 16);
                    secondRegister = Integer.parseInt(String.valueOf(hex.charAt(3)), 16);
                    firstValue = registers[regbankIndex][firstRegister] & 0xFF;
                    secondValue = registers[regbankIndex][secondRegister] & 0xFF;
                    result = firstValue & secondValue;
                    flagZ[regbankIndex] = (result % 256 == 0) ? 1 : 0;
                    flagC[regbankIndex] = (result % 256 == 255) ? 1 : 0;
                    // registers[regbankIndex][firstRegister] = (byte)(result & 0xFF); // TEST does not store result
                    PC++;
                    break;
                }
                case 0x0d000:
                case 0x0f000: {
                    // TEST register, constant
                    firstRegister = Integer.parseInt(String.valueOf(hex.charAt(2)), 16);
                    firstValue = registers[regbankIndex][firstRegister] & 0xFF;
                    secondValue = Integer.parseInt(hex.substring(3), 16);
                    result = firstValue & secondValue;
                    flagZ[regbankIndex] = (result % 256 == 0) ? 1 : 0;
                    flagC[regbankIndex] = (result % 256 == 255) ? 1 : 0;
                    // registers[regbankIndex][firstRegister] = (byte)(result & 0xFF); // TEST does not store result
                    PC++;
                    break;
                }
                case 0x1c000: {
                    // COMPARE register, register
                    firstRegister = Integer.parseInt(String.valueOf(hex.charAt(2)), 16);
                    secondRegister = Integer.parseInt(String.valueOf(hex.charAt(3)), 16);
                    firstValue = registers[regbankIndex][firstRegister] & 0xFF;
                    secondValue = registers[regbankIndex][secondRegister] & 0xFF;
                    result = firstValue - secondValue;
                    flagZ[regbankIndex] = (result % 256 == 0) ? 1 : 0;
                    flagC[regbankIndex] = (result < 0) ? 1 : 0;
                    // registers[regbankIndex][firstRegister] = (byte)(result & 0xFF); // COMPARE does not store result
                    PC++;
                    break;
                }
                case 0x1d000: {
                    // COMPARE register, constant
                    firstRegister = Integer.parseInt(String.valueOf(hex.charAt(2)), 16);
                    firstValue = registers[regbankIndex][firstRegister] & 0xFF;
                    secondValue = Integer.parseInt(hex.substring(3), 16);
                    result = firstValue - secondValue;
                    flagZ[regbankIndex] = (result % 256 == 0) ? 1 : 0;
                    flagC[regbankIndex] = (result < 0) ? 1 : 0;
                    // registers[regbankIndex][firstRegister] = (byte)(result & 0xFF); // COMPARE does not store result
                    PC++;
                    break;
                }
                case 0x1e000: {
                    // COMPARECY register, register
                    firstRegister = Integer.parseInt(String.valueOf(hex.charAt(2)), 16);
                    secondRegister = Integer.parseInt(String.valueOf(hex.charAt(3)), 16);
                    firstValue = registers[regbankIndex][firstRegister] & 0xFF;
                    secondValue = registers[regbankIndex][secondRegister] & 0xFF;
                    result = firstValue - secondValue - flagC[regbankIndex];
                    flagZ[regbankIndex] = (result % 256 == 0) ? 1 : 0;
                    flagC[regbankIndex] = (result < 0) ? 1 : 0;
                    // registers[regbankIndex][firstRegister] = (byte)(result & 0xFF); // COMPARECY does not store result
                    PC++;
                    break;
                }
                case 0x1f000: {
                    // COMPARECY register, constant
                    firstRegister = Integer.parseInt(String.valueOf(hex.charAt(2)), 16);
                    firstValue = registers[regbankIndex][firstRegister] & 0xFF;
                    secondValue = Integer.parseInt(hex.substring(3), 16);
                    result = firstValue - secondValue - flagC[regbankIndex];
                    flagZ[regbankIndex] = (result % 256 == 0) ? 1 : 0;
                    flagC[regbankIndex] = (result < 0) ? 1 : 0;
                    // registers[regbankIndex][firstRegister] = (byte)(result & 0xFF); // COMPARECY does not store result
                    PC++;
                    break;
                }
                case 0x14000: {
                    // Bit-shifting operations...
                    registerIndex = Integer.parseInt(String.valueOf(hex.charAt(2)), 16);
                    registerValue = registers[regbankIndex][registerIndex] & 0xFF;
                    Log.d("PicoBlaze", "DEBUG: Shifting the bits in register s" +
                            Integer.toHexString(registerIndex));
                    switch (hex.substring(3)) {
                        case "06": // SL0
                            registerValue <<= 1;
                            flagC[regbankIndex] = (registerValue > 255) ? 1 : 0;
                            flagZ[regbankIndex] = (registerValue % 256 == 0) ? 1 : 0;
                            break;
                        case "07": // SL1
                            registerValue = (registerValue << 1) + 1;
                            flagC[regbankIndex] = (registerValue > 255) ? 1 : 0;
                            flagZ[regbankIndex] = (registerValue % 256 == 0) ? 1 : 0;
                            break;
                        case "04": // SLX
                            registerValue = (registerValue << 1) + (registerValue % 2);
                            flagC[regbankIndex] = (registerValue > 255) ? 1 : 0;
                            flagZ[regbankIndex] = (registerValue % 256 == 0) ? 1 : 0;
                            break;
                        case "00": // SLA
                            registerValue = (registerValue << 1) + flagC[regbankIndex];
                            flagC[regbankIndex] = (registerValue > 255) ? 1 : 0;
                            flagZ[regbankIndex] = (registerValue % 256 == 0) ? 1 : 0;
                            break;
                        case "02": // RL
                            registerValue = (registerValue << 1) + (registerValue / 128);
                            flagC[regbankIndex] = (registerValue > 255) ? 1 : 0;
                            flagZ[regbankIndex] = (registerValue % 256 == 0) ? 1 : 0;
                            break;
                        case "0e": // SR0
                            flagC[regbankIndex] = registerValue % 2;
                            flagZ[regbankIndex] = (registerValue / 2 == 0) ? 1 : 0;
                            registerValue >>= 1;
                            break;
                        case "0f": // SR1
                            flagC[regbankIndex] = registerValue % 2;
                            flagZ[regbankIndex] = (registerValue / 2 == 0) ? 1 : 0;
                            registerValue = (registerValue >> 1) + 128;
                            break;
                        case "0a": // SRX
                            flagC[regbankIndex] = registerValue % 2;
                            flagZ[regbankIndex] = (registerValue / 2 == 0) ? 1 : 0;
                            registerValue = (registerValue >> 1) + (registerValue / 128) * 128;
                            break;
                        case "08": { // SRA
                            int oldFlagC = flagC[regbankIndex];
                            flagC[regbankIndex] = registerValue % 2;
                            flagZ[regbankIndex] = (registerValue / 2 == 0) ? 1 : 0;
                            registerValue = (registerValue >> 1) | (oldFlagC << 7);
                            break;
                        }
                        case "0c": // RR
                            flagC[regbankIndex] = registerValue % 2;
                            flagZ[regbankIndex] = (registerValue / 2 == 0) ? 1 : 0;
                            registerValue = (registerValue >> 1) + 128 * (registerValue % 2);
                            break;
                        case "80": // HWBUILD (not a bit-shifting operation)
                            flagC[regbankIndex] = 1;
                            break;
                        default:
                            Log.e("PicoBlaze", "The instruction \"" + hex
                                    + "\", assembled from line #" + program.lineNumbers[PC]
                                    + ", hasn't been implemented yet, sorry about that!");
                    }
                    registers[regbankIndex][registerIndex] = (byte) (registerValue & 0xFF);
                    PC++;
                    break;
                }
                case 0x32000:
                    // JUMP Z, label
                    if (flagZ[regbankIndex] != 0)
                        PC = Integer.parseInt(hex.substring(2), 16);
                    else
                        PC++;
                    break;
                case 0x36000:
                    // JUMP NZ, label
                    if (flagZ[regbankIndex] == 0)
                        PC = Integer.parseInt(hex.substring(2), 16);
                    else
                        PC++;
                    break;
                case 0x3a000:
                    // JUMP C, label
                    if (flagC[regbankIndex] != 0)
                        PC = Integer.parseInt(hex.substring(2), 16);
                    else
                        PC++;
                    break;
                case 0x3e000:
                    // JUMP NC, label
                    if (flagC[regbankIndex] == 0)
                        PC = Integer.parseInt(hex.substring(2), 16);
                    else
                        PC++;
                    break;
                case 0x26000: {
                    // JUMP@ (register, register) ;Jump to the address pointed by the registers
                    // (something like function pointers, except that "return" won't work).
                    firstRegister = Integer.parseInt(String.valueOf(hex.charAt(2)), 16);
                    secondRegister = Integer.parseInt(String.valueOf(hex.charAt(3)), 16);
                    firstValue = registers[regbankIndex][firstRegister] & 0xFF;
                    secondValue = registers[regbankIndex][secondRegister] & 0xFF;
                    PC = (firstValue % 16) * 256 + secondValue;
                    break;
                }
                case 0x20000:
                    // CALL functionName
                    callStack.push(PC);
                    PC = Integer.parseInt(hex.substring(2), 16);
                    break;
                case 0x30000:
                    // CALL Z, functionName ;Call the function only if the Zero Flag is set.
                    if (flagZ[regbankIndex] != 0) {
                        callStack.push(PC);
                        PC = Integer.parseInt(hex.substring(2), 16);
                    } else
                        PC++;
                    break;
                case 0x34000:
                    // CALL NZ, functionName ;Call the function only if the Zero Flag is not set.
                    if (flagZ[regbankIndex] == 0) {
                        callStack.push(PC);
                        PC = Integer.parseInt(hex.substring(2), 16);
                    } else
                        PC++;
                    break;
                case 0x38000:
                    // CALL C, functionName ;Call the function only if the Carry Flag is set.
                    if (flagC[regbankIndex] != 0) {
                        callStack.push(PC);
                        PC = Integer.parseInt(hex.substring(2), 16);
                    } else
                        PC++;
                    break;
                case 0x3c000:
                    // CALL NC, functionName ;Call the function only if the Carry Flag is not set.
                    if (flagC[regbankIndex] == 0) {
                        callStack.push(PC);
                        PC = Integer.parseInt(hex.substring(2), 16);
                    } else
                        PC++;
                    break;
                case 0x24000: {
                    // CALL@ (register, register) ;Jump the function pointed by the function
                    // pointer stored in the registers.
                    firstRegister = Integer.parseInt(String.valueOf(hex.charAt(2)), 16);
                    secondRegister = Integer.parseInt(String.valueOf(hex.charAt(3)), 16);
                    firstValue = registers[regbankIndex][firstRegister] & 0xFF;
                    secondValue = registers[regbankIndex][secondRegister] & 0xFF;
                    callStack.push(PC);
                    PC = (firstValue % 16) * 256 + secondValue;
                    break;
                }
                case 0x25000:
                    // RETURN
                    if (!callStack.isEmpty())
                        PC = callStack.pop() + 1;
                    else {
                        Log.i("PicoBlaze", "The program exited!");
                        myTimer.cancel();
                    }
                    break;
                case 0x31000:
                    // RETURN Z ;Return from a function only if the Zero Flag is set.
                    if (flagZ[regbankIndex] != 0) {
                        if (!callStack.isEmpty())
                            PC = callStack.pop() + 1;
                        else {
                            Log.i("PicoBlaze", "The program exited!");
                            myTimer.cancel();
                        }
                    } else
                        PC++;
                    break;
                case 0x35000:
                    // RETURN NZ ;Return from a function only if the Zero Flag is not set.
                    if (flagZ[regbankIndex] == 0) {
                        if (!callStack.isEmpty())
                            PC = callStack.pop() + 1;
                        else {
                            Log.i("PicoBlaze", "The program exited!");
                            myTimer.cancel();
                        }
                    } else
                        PC++;
                    break;
                case 0x39000:
                    // RETURN C ;Return from a function only if the Carry Flag is set.
                    if (flagC[regbankIndex] != 0) {
                        if (!callStack.isEmpty())
                            PC = callStack.pop() + 1;
                        else {
                            Log.i("PicoBlaze", "The program exited!");
                            myTimer.cancel();
                        }
                    } else
                        PC++;
                    break;
                case 0x3d000:
                    // RETURN NC ;Return from a function only if the Carry Flag is not set.
                    if (flagC[regbankIndex] == 0) {
                        if (!callStack.isEmpty())
                            PC = callStack.pop() + 1;
                        else {
                            Log.i("PicoBlaze", "The program exited!");
                            myTimer.cancel();
                        }
                    } else
                        PC++;
                    break;
                case 0x28000:
                    // INTERRUPT ENABLE|DISABLE
                    flagIE = Integer.parseInt(String.valueOf(hex.charAt(4)), 16);
                    PC++;
                    break;
                case 0x29000:
                    // RETURNI ENABLE|DISABLE
                    flagIE = Integer.parseInt(String.valueOf(hex.charAt(4)), 16);
                    if (!callStack.isEmpty())
                        PC = callStack.pop() + 1;
                    else {
                        Log.i("PicoBlaze", "The program exited!");
                        myTimer.cancel();
                    }
                    break;
                default:
                    Log.e("PicoBlaze",
                            "Sorry about that, the simulator currently does not support the instruction \""
                                    + hex + "\" (" + currentDirective + " & " + 0xff000 + " = "
                                    + (currentDirective & 0xff000) + "), assembled from line #"
                                    + program.lineNumbers[PC] + ".");
                    myTimer.cancel();
            }
        } catch (Exception error) {
            Log.e("PicoBlaze", "The simulator crashed! Error: " + error.getMessage());
            myTimer.cancel();
        }
    }

    private static Simulator instance;

    public static Simulator getInstance() {
        if (instance == null)
            instance = new Simulator();
        return instance;
    }
}
