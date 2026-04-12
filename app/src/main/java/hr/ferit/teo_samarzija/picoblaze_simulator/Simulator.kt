/*
* This file was mostly AI-generated based on the simulator written in JavaScript:
* https://picoblaze-simulator.sourceforge.io/simulator.js
* At first, it was written in Java, but then it was converted to Kotlin using the tools
* built into Android studio.
* */

package hr.ferit.teo_samarzija.picoblaze_simulator

import android.util.Log
import android.webkit.WebView
import android.widget.Toast
import java.util.Stack
import java.util.Timer

class Simulator private constructor() {
    @JvmField var terminalOutput: String
    @JvmField var terminalInput: String
    @JvmField var output: ByteArray
    @JvmField var memory: ByteArray
    @JvmField var registers: Array<ByteArray?>
    @JvmField var regbank: Boolean
    @JvmField var PC: Int
    @JvmField var flagZ: IntArray
    @JvmField var flagC: IntArray
    @JvmField var flagIE: Int
    @JvmField var callStack: Stack<Int?>
    @JvmField var currentlyReadCharacterInUART: Int
    @JvmField var myTimer: Timer
    @JvmField var switches: Int = 0

    @JvmField var referenceToTheWebViewInSimulation: WebView? = null

    fun simulateOneInstruction() {
        try {
            // If you are at the end of a program, and there is no "return" there, jump to the
            // beginning of the program. I think that's how PicoBlaze behaves.
            PC = PC % 4096

            val regbankIndex = if (regbank) 1 else 0
            val program = AssembledProgram.getInstance()
            val currentDirective = program.instructions[PC]
            // Format the instruction as a 5-digit hex string for character-based parsing,
            // matching the JavaScript machineCode[PC].hex format.
            val hex = String.format("%05x", currentDirective)

            if (program.breakpoints.contains(program.lineNumbers[PC]) && !program.forbiddenBreakpoints.contains(
                    PC
                )
            ) {
                referenceToTheWebViewInSimulation!!.post(object : Runnable {
                    override fun run() {
                        Toast.makeText(
                            referenceToTheWebViewInSimulation!!.getContext(),
                            "Reached a breakpoint on the line " + program.lineNumbers[PC],
                            Toast.LENGTH_LONG
                        ).show()
                        referenceToTheWebViewInSimulation!!.evaluateJavascript(
                            "document.getElementById(\"playPauseImage\").src=\"play.svg\"; document.getElementById(\"playPauseImage\").alt=\"Play\"; isSimulationPlaying = false;",
                            null
                        )
                    }
                })
                myTimer.cancel()
                return
            }

            val port: Int
            val firstRegister: Int
            val secondRegister: Int
            val firstValue: Int
            val secondValue: Int
            val result: Int
            val value: Int
            val registerIndex: Int
            var registerValue: Int

            // "bennyboy" from "atheistforums.org" thinks my program can be speeded up by using
            // a switch-case instead of the large if-else (that a switch-case would compile into
            // a more efficient assembly code), so it would be interesting to investigate whether
            // that's true: https://atheistforums.org/thread-61911-post-2112817.html#pid2112817
            when (currentDirective and 0xff000) {
                0x00000 -> {
                    // LOAD register, register
                    registers[regbankIndex]!![hex.get(2).toString().toInt(16)] =
                        registers[regbankIndex]!![hex.get(3).toString().toInt(16)]
                    PC++
                }

                0x01000 -> {
                    // LOAD register, constant
                    registers[regbankIndex]!![hex.get(2).toString().toInt(16)] =
                        hex.substring(3).toInt(16).toByte()
                    PC++
                }

                0x17000 -> {
                    // STAR register, constant ;Storing a constant into an inactive register
                    registers[if (regbank) 0 else 1]!![hex.get(2).toString().toInt(16)] =
                        hex.substring(3).toInt(16).toByte()
                    PC++
                }

                0x16000 -> {
                    // STAR register, register ;Copying from an active register into an inactive one.
                    registers[if (regbank) 0 else 1]!![hex.get(2).toString().toInt(16)] =
                        registers[regbankIndex]!![hex.get(3).toString().toInt(16)]
                    PC++
                }

                0x2e000 -> {
                    // STORE register, (register) ;Store the first register at the memory location
                    // where the second register points to.
                    firstRegister = hex.get(2).toString().toInt(16)
                    secondRegister = hex.get(3).toString().toInt(16)
                    memory[registers[regbankIndex]!![secondRegister].toInt() and 0xFF] =
                        registers[regbankIndex]!![firstRegister]
                    PC++
                }

                0x2f000 -> {
                    // STORE register, memory_address ;Copy a register onto a memory address.
                    firstRegister = hex.get(2).toString().toInt(16)
                    memory[hex.substring(3).toInt(16)] =
                        registers[regbankIndex]!![firstRegister]
                    PC++
                }

                0x0a000 -> {
                    // FETCH register, (register) ;Dereference the pointer in the second register.
                    firstRegister = hex.get(2).toString().toInt(16)
                    secondRegister = hex.get(3).toString().toInt(16)
                    registers[regbankIndex]!![firstRegister] =
                        memory[registers[regbankIndex]!![secondRegister].toInt() and 0xFF]
                    PC++
                }

                0x0b000 -> {
                    // FETCH register, memory_address ;Copy the value at memory_address to the register.
                    firstRegister = hex.get(2).toString().toInt(16)
                    registers[regbankIndex]!![firstRegister] =
                        memory[hex.substring(3).toInt(16)]
                    PC++
                }

                0x08000 -> {
                    // INPUT register, (register) ;Read a byte from a port specified by a register.
                    firstRegister = hex.get(2).toString().toInt(16)
                    secondRegister = hex.get(3).toString().toInt(16)
                    port = registers[regbankIndex]!![secondRegister].toInt() and 0xFF
                    if (port == 2 || port == 3) {
                        if (port == 3) {
                            // UART_RX_PORT
                            registers[regbankIndex]!![firstRegister] =
                                if (currentlyReadCharacterInUART < terminalInput.length) (terminalInput.get(
                                    currentlyReadCharacterInUART
                                ).code and 0xFF).toByte() else
                                    0
                            currentlyReadCharacterInUART++
                        } else {
                            // UART_STATUS_PORT
                            registers[regbankIndex]!![firstRegister] =
                                (if (currentlyReadCharacterInUART < terminalInput.length)
                                    8 /*U_RX_D*/
                                else
                                    0).toByte()
                        }
                    } else if (port == 0) {
                        registers[regbankIndex]!![firstRegister] = switches.toByte()
                    } else {
                        // No general input port mechanism in Android version; return 0
                        registers[regbankIndex]!![firstRegister] = 0
                    }
                    PC++
                }

                0x09000 -> {
                    // INPUT register, port_number
                    firstRegister = hex.get(2).toString().toInt(16)
                    port = hex.substring(3).toInt(16)
                    if (port == 2 || port == 3) {
                        if (port == 3) {
                            // UART_RX_PORT
                            registers[regbankIndex]!![firstRegister] =
                                if (currentlyReadCharacterInUART < terminalInput.length) (terminalInput.get(
                                    currentlyReadCharacterInUART
                                ).code and 0xFF).toByte() else
                                    0
                            currentlyReadCharacterInUART++
                        } else {
                            // UART_STATUS_PORT
                            registers[regbankIndex]!![firstRegister] =
                                (if (currentlyReadCharacterInUART < terminalInput.length)
                                    8 /*U_RX_D*/
                                else
                                    0).toByte()
                        }
                    } else if (port == 0) {
                        registers[regbankIndex]!![firstRegister] = switches.toByte()
                    } else {
                        // No general input port mechanism in Android version; return 0
                        registers[regbankIndex]!![firstRegister] = 0
                    }
                    PC++
                }

                0x2c000 -> {
                    // OUTPUT register, (register) ;Output the result of the first register to
                    // the port specified by the second register.
                    firstRegister = hex.get(2).toString().toInt(16)
                    secondRegister = hex.get(3).toString().toInt(16)
                    port = registers[regbankIndex]!![secondRegister].toInt() and 0xFF
                    value = registers[regbankIndex]!![firstRegister].toInt() and 0xFF
                    if (port == 3 || port == 4) {
                        if (port == 3) {
                            // UART_TX_PORT
                            terminalOutput += value.toChar().toString()
                            referenceToTheWebViewInSimulation!!.post(object : Runnable {
                                override fun run() {
                                    referenceToTheWebViewInSimulation!!.evaluateJavascript(
                                        "document.getElementById(\"UART_output\").innerHTML=\"" + terminalOutput.replace(
                                            "\n".toRegex(),
                                            "\\\\n"
                                        ) + "\";", null
                                    )
                                }
                            })
                        } else {
                            // UART_RESET_PORT
                            terminalOutput = ""
                            referenceToTheWebViewInSimulation!!.post(object : Runnable {
                                override fun run() {
                                    referenceToTheWebViewInSimulation!!.evaluateJavascript(
                                        "document.getElementById(\"UART_output\").innerHTML=\"" + terminalOutput.replace(
                                            "\n".toRegex(),
                                            "\\\\n"
                                        ) + "\";", null
                                    )
                                }
                            })
                        }
                    } else if (port == 0) {
                        output[port] = value.toByte()
                        referenceToTheWebViewInSimulation!!.post(object : Runnable {
                            override fun run() {
                                referenceToTheWebViewInSimulation!!.evaluateJavascript(
                                    "updateTheLEDs();",
                                    null
                                )
                            }
                        })
                    } else if (port == 1) {
                        output[port] = value.toByte()
                        referenceToTheWebViewInSimulation!!.post(object : Runnable {
                            override fun run() {
                                referenceToTheWebViewInSimulation!!.evaluateJavascript(
                                    "displayHexadecimalNumber(document.getElementById(\"sevenSegmentDisplay0\"), " + (value shr 4) + "); displayHexadecimalNumber(document.getElementById(\"sevenSegmentDisplay1\"), " + (value and 0xf) + ");",
                                    null
                                )
                            }
                        })
                    } else if (port == 2) {
                        output[port] = value.toByte()
                        referenceToTheWebViewInSimulation!!.post(object : Runnable {
                            override fun run() {
                                referenceToTheWebViewInSimulation!!.evaluateJavascript(
                                    "displayHexadecimalNumber(document.getElementById(\"sevenSegmentDisplay2\"), " + (value shr 4) + "); displayHexadecimalNumber(document.getElementById(\"sevenSegmentDisplay3\"), " + (value and 0xf) + ");",
                                    null
                                )
                            }
                        })
                    } else {
                        output[port] = value.toByte()
                    }
                    PC++
                }

                0x2d000 -> {
                    // OUTPUT register, port_number
                    firstRegister = hex.get(2).toString().toInt(16)
                    port = hex.substring(3).toInt(16)
                    value = registers[regbankIndex]!![firstRegister].toInt() and 0xFF
                    if (port == 3 || port == 4) {
                        if (port == 3)  // UART_TX_PORT
                            terminalOutput += value.toChar().toString()
                        else  // UART_RESET_PORT
                            terminalOutput = ""
                        referenceToTheWebViewInSimulation!!.post(object : Runnable {
                            override fun run() {
                                referenceToTheWebViewInSimulation!!.evaluateJavascript(
                                    "document.getElementById(\"UART_output\").innerHTML=\"" + terminalOutput.replace(
                                        "\n".toRegex(),
                                        "\\\\n"
                                    ) + "\";", null
                                )
                            }
                        })
                    } else if (port == 0) {
                        output[port] = value.toByte()
                        referenceToTheWebViewInSimulation!!.post(object : Runnable {
                            override fun run() {
                                referenceToTheWebViewInSimulation!!.evaluateJavascript(
                                    "updateTheLEDs();",
                                    null
                                )
                            }
                        })
                    } else if (port == 1) {
                        output[port] = value.toByte()
                        referenceToTheWebViewInSimulation!!.post(object : Runnable {
                            override fun run() {
                                referenceToTheWebViewInSimulation!!.evaluateJavascript(
                                    "displayHexadecimalNumber(document.getElementById(\"sevenSegmentDisplay0\"), " + (value shr 4) + "); displayHexadecimalNumber(document.getElementById(\"sevenSegmentDisplay1\"), " + (value and 0xf) + ");",
                                    null
                                )
                            }
                        })
                    } else if (port == 2) {
                        output[port] = value.toByte()
                        referenceToTheWebViewInSimulation!!.post(object : Runnable {
                            override fun run() {
                                referenceToTheWebViewInSimulation!!.evaluateJavascript(
                                    "displayHexadecimalNumber(document.getElementById(\"sevenSegmentDisplay2\"), " + (value shr 4) + "); displayHexadecimalNumber(document.getElementById(\"sevenSegmentDisplay3\"), " + (value and 0xf) + ");",
                                    null
                                )
                            }
                        })
                    } else {
                        output[port] = value.toByte()
                    }
                    PC++
                }

                0x2b000 -> {
                    // OUTPUTK constant, port_number
                    value = hex.substring(2, 4).toInt(16)
                    port = hex.get(4).toString().toInt(16)
                    if (port == 3 || port == 4) {
                        if (port == 3)  // UART_TX_PORT
                            terminalOutput += value.toChar().toString()
                        else  // UART_RESET_PORT
                            terminalOutput = ""
                        referenceToTheWebViewInSimulation!!.post(object : Runnable {
                            override fun run() {
                                referenceToTheWebViewInSimulation!!.evaluateJavascript(
                                    "document.getElementById(\"UART_output\").innerHTML=\"" + terminalOutput.replace(
                                        "\n".toRegex(),
                                        "\\\\n"
                                    ) + "\";", null
                                )
                            }
                        })
                    } else if (port == 0) {
                        output[port] = value.toByte()
                        referenceToTheWebViewInSimulation!!.post(object : Runnable {
                            override fun run() {
                                referenceToTheWebViewInSimulation!!.evaluateJavascript(
                                    "updateTheLEDs();",
                                    null
                                )
                            }
                        })
                    } else if (port == 1) {
                        output[port] = value.toByte()
                        referenceToTheWebViewInSimulation!!.post(object : Runnable {
                            override fun run() {
                                referenceToTheWebViewInSimulation!!.evaluateJavascript(
                                    "displayHexadecimalNumber(document.getElementById(\"sevenSegmentDisplay0\"), " + (value shr 4) + "); displayHexadecimalNumber(document.getElementById(\"sevenSegmentDisplay1\"), " + (value and 0xf) + ");",
                                    null
                                )
                            }
                        })
                    } else if (port == 2) {
                        output[port] = value.toByte()
                        referenceToTheWebViewInSimulation!!.post(object : Runnable {
                            override fun run() {
                                referenceToTheWebViewInSimulation!!.evaluateJavascript(
                                    "displayHexadecimalNumber(document.getElementById(\"sevenSegmentDisplay2\"), " + (value shr 4) + "); displayHexadecimalNumber(document.getElementById(\"sevenSegmentDisplay3\"), " + (value and 0xf) + ");",
                                    null
                                )
                            }
                        })
                    } else {
                        output[port] = value.toByte()
                    }
                    PC++
                }

                0x37000 -> {
                    // REGBANK A or REGBANK B
                    if ((currentDirective and 0x00ff0) != 0 || (currentDirective and 0xf) > 1) {
                        Log.e(
                            "PicoBlaze",
                            ("Sorry about that, the simulator currently does not support the instruction \""
                                    + hex + "\" (" + currentDirective + " & " + 0xff000 + " = "
                                    + (currentDirective and 0xff000) + "), assembled from line #"
                                    + program.lineNumbers[PC] + ".")
                        )
                        referenceToTheWebViewInSimulation!!.post(object : Runnable {
                            override fun run() {
                                referenceToTheWebViewInSimulation!!.evaluateJavascript(
                                    "document.getElementById(\"playPauseButton\").click()",
                                    null
                                )
                                Toast.makeText(referenceToTheWebViewInSimulation!!.context, ("Sorry about that, the simulator currently does not support the instruction \""
                                        + hex + "\" (" + currentDirective + " & " + 0xff000 + " = "
                                        + (currentDirective and 0xff000) + "), assembled from line #"
                                        + program.lineNumbers[PC] + "."), Toast.LENGTH_LONG).show()
                            }
                        })
                        myTimer.cancel()
                        return
                    }
                    regbank = (currentDirective % 2 != 0)
                    PC++
                }

                0x22000 ->                     // JUMP label
                    PC = hex.substring(2).toInt(16)

                0x10000 -> {
                    // ADD register, register
                    firstRegister = hex.get(2).toString().toInt(16)
                    secondRegister = hex.get(3).toString().toInt(16)
                    firstValue = registers[regbankIndex]!![firstRegister].toInt() and 0xFF
                    secondValue = registers[regbankIndex]!![secondRegister].toInt() and 0xFF
                    result = firstValue + secondValue
                    flagZ[regbankIndex] = if (result % 256 == 0) 1 else 0
                    flagC[regbankIndex] = if (result > 255) 1 else 0
                    registers[regbankIndex]!![firstRegister] = (result and 0xFF).toByte()
                    PC++
                }

                0x11000 -> {
                    // ADD register, constant
                    firstRegister = hex.get(2).toString().toInt(16)
                    firstValue = registers[regbankIndex]!![firstRegister].toInt() and 0xFF
                    secondValue = hex.substring(3).toInt(16)
                    result = firstValue + secondValue
                    flagZ[regbankIndex] = if (result % 256 == 0) 1 else 0
                    flagC[regbankIndex] = if (result > 255) 1 else 0
                    registers[regbankIndex]!![firstRegister] = (result and 0xFF).toByte()
                    PC++
                }

                0x12000 -> {
                    // ADDCY register, register
                    firstRegister = hex.get(2).toString().toInt(16)
                    secondRegister = hex.get(3).toString().toInt(16)
                    firstValue = registers[regbankIndex]!![firstRegister].toInt() and 0xFF
                    secondValue = registers[regbankIndex]!![secondRegister].toInt() and 0xFF
                    result = firstValue + secondValue + flagC[regbankIndex]
                    flagZ[regbankIndex] = if (result % 256 == 0) 1 else 0
                    flagC[regbankIndex] = if (result > 255) 1 else 0
                    registers[regbankIndex]!![firstRegister] = (result and 0xFF).toByte()
                    PC++
                }

                0x13000 -> {
                    // ADDCY register, constant
                    firstRegister = hex.get(2).toString().toInt(16)
                    firstValue = registers[regbankIndex]!![firstRegister].toInt() and 0xFF
                    secondValue = hex.substring(3).toInt(16)
                    result = firstValue + secondValue + flagC[regbankIndex]
                    flagZ[regbankIndex] = if (result % 256 == 0) 1 else 0
                    flagC[regbankIndex] = if (result > 255) 1 else 0
                    registers[regbankIndex]!![firstRegister] = (result and 0xFF).toByte()
                    PC++
                }

                0x18000 -> {
                    // SUB register, register
                    firstRegister = hex.get(2).toString().toInt(16)
                    secondRegister = hex.get(3).toString().toInt(16)
                    firstValue = registers[regbankIndex]!![firstRegister].toInt() and 0xFF
                    secondValue = registers[regbankIndex]!![secondRegister].toInt() and 0xFF
                    result = firstValue - secondValue
                    flagZ[regbankIndex] = if (result % 256 == 0) 1 else 0
                    flagC[regbankIndex] = if (result < 0) 1 else 0
                    registers[regbankIndex]!![firstRegister] = (result and 0xFF).toByte()
                    PC++
                }

                0x19000 -> {
                    // SUB register, constant
                    firstRegister = hex.get(2).toString().toInt(16)
                    firstValue = registers[regbankIndex]!![firstRegister].toInt() and 0xFF
                    secondValue = hex.substring(3).toInt(16)
                    result = firstValue - secondValue
                    flagZ[regbankIndex] = if (result % 256 == 0) 1 else 0
                    flagC[regbankIndex] = if (result < 0) 1 else 0
                    registers[regbankIndex]!![firstRegister] = (result and 0xFF).toByte()
                    PC++
                }

                0x1a000 -> {
                    // SUBCY register, register
                    firstRegister = hex.get(2).toString().toInt(16)
                    secondRegister = hex.get(3).toString().toInt(16)
                    firstValue = registers[regbankIndex]!![firstRegister].toInt() and 0xFF
                    secondValue = registers[regbankIndex]!![secondRegister].toInt() and 0xFF
                    result = firstValue - secondValue - flagC[regbankIndex]
                    flagZ[regbankIndex] = if (result % 256 == 0) 1 else 0
                    flagC[regbankIndex] = if (result < 0) 1 else 0
                    registers[regbankIndex]!![firstRegister] = (result and 0xFF).toByte()
                    PC++
                }

                0x1b000 -> {
                    // SUBCY register, constant
                    firstRegister = hex.get(2).toString().toInt(16)
                    firstValue = registers[regbankIndex]!![firstRegister].toInt() and 0xFF
                    secondValue = hex.substring(3).toInt(16)
                    result = firstValue - secondValue - flagC[regbankIndex]
                    flagZ[regbankIndex] = if (result % 256 == 0) 1 else 0
                    flagC[regbankIndex] = if (result < 0) 1 else 0
                    registers[regbankIndex]!![firstRegister] = (result and 0xFF).toByte()
                    PC++
                }

                0x02000 -> {
                    // AND register, register
                    firstRegister = hex.get(2).toString().toInt(16)
                    secondRegister = hex.get(3).toString().toInt(16)
                    firstValue = registers[regbankIndex]!![firstRegister].toInt() and 0xFF
                    secondValue = registers[regbankIndex]!![secondRegister].toInt() and 0xFF
                    result = firstValue and secondValue
                    flagZ[regbankIndex] = if (result % 256 == 0) 1 else 0
                    flagC[regbankIndex] = if (result % 256 == 255) 1 else 0
                    registers[regbankIndex]!![firstRegister] = (result and 0xFF).toByte()
                    PC++
                }

                0x03000 -> {
                    // AND register, constant
                    firstRegister = hex.get(2).toString().toInt(16)
                    firstValue = registers[regbankIndex]!![firstRegister].toInt() and 0xFF
                    secondValue = hex.substring(3).toInt(16)
                    result = firstValue and secondValue
                    flagZ[regbankIndex] = if (result % 256 == 0) 1 else 0
                    flagC[regbankIndex] = if (result % 256 == 255) 1 else 0
                    registers[regbankIndex]!![firstRegister] = (result and 0xFF).toByte()
                    PC++
                }

                0x04000 -> {
                    // OR register, register
                    firstRegister = hex.get(2).toString().toInt(16)
                    secondRegister = hex.get(3).toString().toInt(16)
                    firstValue = registers[regbankIndex]!![firstRegister].toInt() and 0xFF
                    secondValue = registers[regbankIndex]!![secondRegister].toInt() and 0xFF
                    result = firstValue or secondValue
                    flagZ[regbankIndex] = if (result % 256 == 0) 1 else 0
                    flagC[regbankIndex] = if (result % 256 == 255) 1 else 0
                    registers[regbankIndex]!![firstRegister] = (result and 0xFF).toByte()
                    PC++
                }

                0x05000 -> {
                    // OR register, constant
                    firstRegister = hex.get(2).toString().toInt(16)
                    firstValue = registers[regbankIndex]!![firstRegister].toInt() and 0xFF
                    secondValue = hex.substring(3).toInt(16)
                    result = firstValue or secondValue
                    flagZ[regbankIndex] = if (result % 256 == 0) 1 else 0
                    flagC[regbankIndex] = if (result % 256 == 255) 1 else 0
                    registers[regbankIndex]!![firstRegister] = (result and 0xFF).toByte()
                    PC++
                }

                0x06000 -> {
                    // XOR register, register
                    firstRegister = hex.get(2).toString().toInt(16)
                    secondRegister = hex.get(3).toString().toInt(16)
                    firstValue = registers[regbankIndex]!![firstRegister].toInt() and 0xFF
                    secondValue = registers[regbankIndex]!![secondRegister].toInt() and 0xFF
                    result = firstValue xor secondValue
                    flagZ[regbankIndex] = if (result % 256 == 0) 1 else 0
                    flagC[regbankIndex] = if (result % 256 == 255) 1 else 0
                    registers[regbankIndex]!![firstRegister] = (result and 0xFF).toByte()
                    PC++
                }

                0x07000 -> {
                    // XOR register, constant
                    firstRegister = hex.get(2).toString().toInt(16)
                    firstValue = registers[regbankIndex]!![firstRegister].toInt() and 0xFF
                    secondValue = hex.substring(3).toInt(16)
                    result = firstValue xor secondValue
                    flagZ[regbankIndex] = if (result % 256 == 0) 1 else 0
                    flagC[regbankIndex] = if (result % 256 == 255) 1 else 0
                    registers[regbankIndex]!![firstRegister] = (result and 0xFF).toByte()
                    PC++
                }

                0x0c000, 0x0e000 -> {
                    // TEST register, register ;The same as "AND", but does not store the result
                    // (only the flags). I am not sure if there is a difference between "0c" and
                    // "0e", they appear to be the same.
                    firstRegister = hex.get(2).toString().toInt(16)
                    secondRegister = hex.get(3).toString().toInt(16)
                    firstValue = registers[regbankIndex]!![firstRegister].toInt() and 0xFF
                    secondValue = registers[regbankIndex]!![secondRegister].toInt() and 0xFF
                    result = firstValue and secondValue
                    flagZ[regbankIndex] = if (result % 256 == 0) 1 else 0
                    flagC[regbankIndex] = if (result % 256 == 255) 1 else 0
                    // registers[regbankIndex][firstRegister] = (byte)(result & 0xFF); // TEST does not store result
                    PC++
                }

                0x0d000, 0x0f000 -> {
                    // TEST register, constant
                    firstRegister = hex.get(2).toString().toInt(16)
                    firstValue = registers[regbankIndex]!![firstRegister].toInt() and 0xFF
                    secondValue = hex.substring(3).toInt(16)
                    result = firstValue and secondValue
                    flagZ[regbankIndex] = if (result % 256 == 0) 1 else 0
                    flagC[regbankIndex] = if (result % 256 == 255) 1 else 0
                    // registers[regbankIndex][firstRegister] = (byte)(result & 0xFF); // TEST does not store result
                    PC++
                }

                0x1c000 -> {
                    // COMPARE register, register
                    firstRegister = hex.get(2).toString().toInt(16)
                    secondRegister = hex.get(3).toString().toInt(16)
                    firstValue = registers[regbankIndex]!![firstRegister].toInt() and 0xFF
                    secondValue = registers[regbankIndex]!![secondRegister].toInt() and 0xFF
                    result = firstValue - secondValue
                    flagZ[regbankIndex] = if (result % 256 == 0) 1 else 0
                    flagC[regbankIndex] = if (result < 0) 1 else 0
                    // registers[regbankIndex][firstRegister] = (byte)(result & 0xFF); // COMPARE does not store result
                    PC++
                }

                0x1d000 -> {
                    // COMPARE register, constant
                    firstRegister = hex.get(2).toString().toInt(16)
                    firstValue = registers[regbankIndex]!![firstRegister].toInt() and 0xFF
                    secondValue = hex.substring(3).toInt(16)
                    result = firstValue - secondValue
                    flagZ[regbankIndex] = if (result % 256 == 0) 1 else 0
                    flagC[regbankIndex] = if (result < 0) 1 else 0
                    // registers[regbankIndex][firstRegister] = (byte)(result & 0xFF); // COMPARE does not store result
                    PC++
                }

                0x1e000 -> {
                    // COMPARECY register, register
                    firstRegister = hex.get(2).toString().toInt(16)
                    secondRegister = hex.get(3).toString().toInt(16)
                    firstValue = registers[regbankIndex]!![firstRegister].toInt() and 0xFF
                    secondValue = registers[regbankIndex]!![secondRegister].toInt() and 0xFF
                    result = firstValue - secondValue - flagC[regbankIndex]
                    flagZ[regbankIndex] = if (result % 256 == 0) 1 else 0
                    flagC[regbankIndex] = if (result < 0) 1 else 0
                    // registers[regbankIndex][firstRegister] = (byte)(result & 0xFF); // COMPARECY does not store result
                    PC++
                }

                0x1f000 -> {
                    // COMPARECY register, constant
                    firstRegister = hex.get(2).toString().toInt(16)
                    firstValue = registers[regbankIndex]!![firstRegister].toInt() and 0xFF
                    secondValue = hex.substring(3).toInt(16)
                    result = firstValue - secondValue - flagC[regbankIndex]
                    flagZ[regbankIndex] = if (result % 256 == 0) 1 else 0
                    flagC[regbankIndex] = if (result < 0) 1 else 0
                    // registers[regbankIndex][firstRegister] = (byte)(result & 0xFF); // COMPARECY does not store result
                    PC++
                }

                0x14000 -> {
                    // Bit-shifting operations...
                    registerIndex = hex.get(2).toString().toInt(16)
                    registerValue = registers[regbankIndex]!![registerIndex].toInt() and 0xFF
                    Log.d(
                        "PicoBlaze", "DEBUG: Shifting the bits in register s" +
                                Integer.toHexString(registerIndex)
                    )
                    when (hex.substring(3)) {
                        "06" -> {
                            registerValue = registerValue shl 1
                            flagC[regbankIndex] = if (registerValue > 255) 1 else 0
                            flagZ[regbankIndex] = if (registerValue % 256 == 0) 1 else 0
                        }

                        "07" -> {
                            registerValue = (registerValue shl 1) + 1
                            flagC[regbankIndex] = if (registerValue > 255) 1 else 0
                            flagZ[regbankIndex] = if (registerValue % 256 == 0) 1 else 0
                        }

                        "04" -> {
                            registerValue = (registerValue shl 1) + (registerValue % 2)
                            flagC[regbankIndex] = if (registerValue > 255) 1 else 0
                            flagZ[regbankIndex] = if (registerValue % 256 == 0) 1 else 0
                        }

                        "00" -> {
                            registerValue = (registerValue shl 1) + flagC[regbankIndex]
                            flagC[regbankIndex] = if (registerValue > 255) 1 else 0
                            flagZ[regbankIndex] = if (registerValue % 256 == 0) 1 else 0
                        }

                        "02" -> {
                            registerValue = (registerValue shl 1) + (registerValue / 128)
                            flagC[regbankIndex] = if (registerValue > 255) 1 else 0
                            flagZ[regbankIndex] = if (registerValue % 256 == 0) 1 else 0
                        }

                        "0e" -> {
                            flagC[regbankIndex] = registerValue % 2
                            flagZ[regbankIndex] = if (registerValue / 2 == 0) 1 else 0
                            registerValue = registerValue shr 1
                        }

                        "0f" -> {
                            flagC[regbankIndex] = registerValue % 2
                            flagZ[regbankIndex] = if (registerValue / 2 == 0) 1 else 0
                            registerValue = (registerValue shr 1) + 128
                        }

                        "0a" -> {
                            flagC[regbankIndex] = registerValue % 2
                            flagZ[regbankIndex] = if (registerValue / 2 == 0) 1 else 0
                            registerValue = (registerValue shr 1) + (registerValue / 128) * 128
                        }

                        "08" -> {
                            // SRA
                            val oldFlagC = flagC[regbankIndex]
                            flagC[regbankIndex] = registerValue % 2
                            flagZ[regbankIndex] = if (registerValue / 2 == 0) 1 else 0
                            registerValue =
                                (registerValue shr 1) or (oldFlagC shl 7) // https://github.com/FlatAssembler/PicoBlaze_Simulator_in_JS/issues/9
                        }

                        "0c" -> {
                            flagC[regbankIndex] = registerValue % 2
                            flagZ[regbankIndex] = if (registerValue / 2 == 0) 1 else 0
                            registerValue = (registerValue shr 1) + 128 * (registerValue % 2)
                        }

                        "80" -> flagC[regbankIndex] = 1
                        else -> {
                            Log.e(
                                "PicoBlaze", ("The instruction \"" + hex
                                        + "\", assembled from line #" + program.lineNumbers[PC]
                                        + ", hasn't been implemented yet, sorry about that!")
                            )
                            referenceToTheWebViewInSimulation!!.post(object : Runnable {
                                override fun run() {
                                    referenceToTheWebViewInSimulation!!.evaluateJavascript(
                                        "document.getElementById(\"playPauseButton\").click()",
                                        null
                                    )
                                    Toast.makeText(referenceToTheWebViewInSimulation!!.context,("The instruction \"" + hex
                                            + "\", assembled from line #" + program.lineNumbers[PC]
                                            + ", hasn't been implemented yet, sorry about that!"), Toast.LENGTH_LONG).show()
                                }
                            })
                            myTimer.cancel()
                            return
                        }
                    }
                    registers[regbankIndex]!![registerIndex] = (registerValue and 0xFF).toByte()
                    PC++
                }

                0x32000 ->                     // JUMP Z, label
                    if (flagZ[regbankIndex] != 0) PC = hex.substring(2).toInt(16)
                    else PC++

                0x36000 ->                     // JUMP NZ, label
                    if (flagZ[regbankIndex] == 0) PC = hex.substring(2).toInt(16)
                    else PC++

                0x3a000 ->                     // JUMP C, label
                    if (flagC[regbankIndex] != 0) PC = hex.substring(2).toInt(16)
                    else PC++

                0x3e000 ->                     // JUMP NC, label
                    if (flagC[regbankIndex] == 0) PC = hex.substring(2).toInt(16)
                    else PC++

                0x26000 -> {
                    // JUMP@ (register, register) ;Jump to the address pointed by the registers
                    // (something like function pointers, except that "return" won't work).
                    firstRegister = hex.get(2).toString().toInt(16)
                    secondRegister = hex.get(3).toString().toInt(16)
                    firstValue = registers[regbankIndex]!![firstRegister].toInt() and 0xFF
                    secondValue = registers[regbankIndex]!![secondRegister].toInt() and 0xFF
                    PC = (firstValue % 16) * 256 + secondValue
                }

                0x20000 -> {
                    // CALL functionName
                    callStack.push(PC)
                    PC = hex.substring(2).toInt(16)
                }

                0x30000 ->                     // CALL Z, functionName ;Call the function only if the Zero Flag is set.
                    if (flagZ[regbankIndex] != 0) {
                        callStack.push(PC)
                        PC = hex.substring(2).toInt(16)
                    } else PC++

                0x34000 ->                     // CALL NZ, functionName ;Call the function only if the Zero Flag is not set.
                    if (flagZ[regbankIndex] == 0) {
                        callStack.push(PC)
                        PC = hex.substring(2).toInt(16)
                    } else PC++

                0x38000 ->                     // CALL C, functionName ;Call the function only if the Carry Flag is set.
                    if (flagC[regbankIndex] != 0) {
                        callStack.push(PC)
                        PC = hex.substring(2).toInt(16)
                    } else PC++

                0x3c000 ->                     // CALL NC, functionName ;Call the function only if the Carry Flag is not set.
                    if (flagC[regbankIndex] == 0) {
                        callStack.push(PC)
                        PC = hex.substring(2).toInt(16)
                    } else PC++

                0x24000 -> {
                    // CALL@ (register, register) ;Jump the function pointed by the function
                    // pointer stored in the registers.
                    firstRegister = hex.get(2).toString().toInt(16)
                    secondRegister = hex.get(3).toString().toInt(16)
                    firstValue = registers[regbankIndex]!![firstRegister].toInt() and 0xFF
                    secondValue = registers[regbankIndex]!![secondRegister].toInt() and 0xFF
                    callStack.push(PC)
                    PC = (firstValue % 16) * 256 + secondValue
                }

                0x25000 ->                     // RETURN
                    if (!callStack.isEmpty()) PC = callStack.pop()!! + 1
                    else {
                        Log.i("PicoBlaze", "The program exited!")
                        referenceToTheWebViewInSimulation!!.post(object : Runnable {
                            override fun run() {
                                referenceToTheWebViewInSimulation!!.evaluateJavascript(
                                    "document.getElementById(\"playPauseButton\").click()",
                                    null
                                )
                                Toast.makeText(referenceToTheWebViewInSimulation!!.context,"The program exited!",
                                    Toast.LENGTH_LONG).show()
                            }
                        })
                        myTimer.cancel()
                        return
                    }

                0x31000 ->                     // RETURN Z ;Return from a function only if the Zero Flag is set.
                    if (flagZ[regbankIndex] != 0) {
                        if (!callStack.isEmpty()) PC = callStack.pop()!! + 1
                        else {
                            Log.i("PicoBlaze", "The program exited!")
                            referenceToTheWebViewInSimulation!!.post(object : Runnable {
                                override fun run() {
                                    referenceToTheWebViewInSimulation!!.evaluateJavascript(
                                        "document.getElementById(\"playPauseButton\").click()",
                                        null
                                    )
                                    Toast.makeText(referenceToTheWebViewInSimulation!!.context,"The program exited!",
                                        Toast.LENGTH_LONG).show()
                                }
                            })
                            myTimer.cancel()
                            return
                        }
                    } else PC++

                0x35000 ->                     // RETURN NZ ;Return from a function only if the Zero Flag is not set.
                    if (flagZ[regbankIndex] == 0) {
                        if (!callStack.isEmpty()) PC = callStack.pop()!! + 1
                        else {
                            Log.i("PicoBlaze", "The program exited!")
                            referenceToTheWebViewInSimulation!!.post(object : Runnable {
                                override fun run() {
                                    referenceToTheWebViewInSimulation!!.evaluateJavascript(
                                        "document.getElementById(\"playPauseButton\").click()",
                                        null
                                    )
                                    Toast.makeText(referenceToTheWebViewInSimulation!!.context,"The program exited!",
                                        Toast.LENGTH_LONG).show()
                                }
                            })
                            myTimer.cancel()
                            return
                        }
                    } else PC++

                0x39000 ->                     // RETURN C ;Return from a function only if the Carry Flag is set.
                    if (flagC[regbankIndex] != 0) {
                        if (!callStack.isEmpty()) PC = callStack.pop()!! + 1
                        else {
                            Log.i("PicoBlaze", "The program exited!")
                            referenceToTheWebViewInSimulation!!.post(object : Runnable {
                                override fun run() {
                                    referenceToTheWebViewInSimulation!!.evaluateJavascript(
                                        "document.getElementById(\"playPauseButton\").click()",
                                        null
                                    )
                                    Toast.makeText(
                                        referenceToTheWebViewInSimulation!!.context,"The program exited!",
                                        Toast.LENGTH_LONG).show()
                                }
                            })
                            myTimer.cancel()
                            return
                        }
                    } else PC++

                0x3d000 ->                     // RETURN NC ;Return from a function only if the Carry Flag is not set.
                    if (flagC[regbankIndex] == 0) {
                        if (!callStack.isEmpty()) PC = callStack.pop()!! + 1
                        else {
                            Log.i("PicoBlaze", "The program exited!")
                            referenceToTheWebViewInSimulation!!.post(object : Runnable {
                                override fun run() {
                                    referenceToTheWebViewInSimulation!!.evaluateJavascript(
                                        "document.getElementById(\"playPauseButton\").click()",
                                        null
                                    )
                                    Toast.makeText(referenceToTheWebViewInSimulation!!.context,"The program exited!",
                                        Toast.LENGTH_LONG).show()
                                }
                            })
                            myTimer.cancel()
                            return
                        }
                    } else PC++

                0x28000 -> {
                    // INTERRUPT ENABLE|DISABLE
                    flagIE = hex.get(4).toString().toInt(16)
                    PC++
                }

                0x29000 -> {
                    // RETURNI ENABLE|DISABLE
                    flagIE = hex.get(4).toString().toInt(16)
                    if (!callStack.isEmpty()) PC = callStack.pop()!! + 1
                    else {
                        Log.i("PicoBlaze", "The program exited!")
                        referenceToTheWebViewInSimulation!!.post(object : Runnable {
                            override fun run() {
                                referenceToTheWebViewInSimulation!!.evaluateJavascript(
                                    "document.getElementById(\"playPauseButton\").click()",
                                    null
                                )
                                Toast.makeText(referenceToTheWebViewInSimulation!!.context,"The program exited!",
                                    Toast.LENGTH_LONG).show()
                            }
                        })
                        myTimer.cancel()
                        return
                    }
                }

                else -> {
                    Log.e(
                        "PicoBlaze",
                        ("Sorry about that, the simulator currently does not support the instruction \""
                                + hex + "\" (" + currentDirective + " & " + 0xff000 + " = "
                                + (currentDirective and 0xff000) + "), assembled from line #"
                                + program.lineNumbers[PC] + ".")
                    )
                    referenceToTheWebViewInSimulation!!.post(object : Runnable {
                        override fun run() {
                            referenceToTheWebViewInSimulation!!.evaluateJavascript(
                                "document.getElementById(\"playPauseButton\").click()",
                                null
                            )
                            Toast.makeText(referenceToTheWebViewInSimulation!!.context, ("Sorry about that, the simulator currently does not support the instruction \""
                                    + hex + "\" (" + currentDirective + " & " + 0xff000 + " = "
                                    + (currentDirective and 0xff000) + "), assembled from line #"
                                    + program.lineNumbers[PC] + "."), Toast.LENGTH_LONG).show()
                        }
                    })
                    myTimer.cancel()
                    return
                }
            }
        } catch (error: Exception) {
            Log.e("PicoBlaze", "The simulator crashed! Error: " + error.message)
            referenceToTheWebViewInSimulation!!.post(object : Runnable {
                override fun run() {
                    referenceToTheWebViewInSimulation!!.evaluateJavascript(
                        "document.getElementById(\"playPauseButton\").click()",
                        null
                    )
                }
            })
            myTimer.cancel()
            return
        }
    }

    init {
        terminalOutput = AssembledProgram.getInstance().terminalOutputDuringAssembly
        output = ByteArray(256)
        terminalInput = ""
        memory = ByteArray(256)
        registers = arrayOfNulls<ByteArray>(2)
        registers[0] = ByteArray(16)
        registers[1] = ByteArray(16)
        regbank = false
        PC = 0
        flagZ = intArrayOf(0, 0)
        flagC = intArrayOf(0, 0)
        flagIE = 0
        callStack = Stack<Int?>()
        currentlyReadCharacterInUART = 0
        myTimer = Timer()
    }

    companion object instance {
        @JvmStatic
        var instance: Simulator? = null
            get() {
                if (field == null) field = Simulator()
                return field
            }
            private set
    }
}
