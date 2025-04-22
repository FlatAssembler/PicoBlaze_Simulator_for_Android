package hr.ferit.teo_samarzija.picoblaze_simulator;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class WebAppInterface {
    public int[] instructions, lineNumbers;
    Context mContext;
    public String assemblyCode;
    AssembledProgram assembledProgram;
    public String textDisplayedDuringAssembly;
    public java.util.TreeSet<Integer> forbiddenBreakpoints;

    WebAppInterface(Context context) {
        mContext = context;
        assembledProgram = AssembledProgram.getInstance();
        instructions = assembledProgram.instructions;
        lineNumbers = assembledProgram.lineNumbers;
        assemblyCode = assembledProgram.assemblyCode;
        textDisplayedDuringAssembly = assembledProgram.terminalOutputDuringAssembly;
        forbiddenBreakpoints = assembledProgram.forbiddenBreakpoints;
    }

    @JavascriptInterface
    public void setInstructionAtAddress(int address, int instruction) {
        if (address < 0 || address >= 1 << 12) {
            Toast
                    .makeText(mContext,
                            "The JavaScript program tried to write at the address " +
                                    Integer.toHexString(address) + ", which is invalid!",
                            Toast.LENGTH_LONG)
                    .show();
            return;
        }
        if (instruction < 0 || instruction >= 1 << 18) {
            Toast
                    .makeText(
                            mContext,
                            "The JavaScript program tried to write a machine-code directive " +
                                    Integer.toHexString(instruction) + ", which is invalid!",
                            Toast.LENGTH_LONG)
                    .show();
            return;
        }
        instructions[address] = instruction;
        assembledProgram.instructions[address] = instruction;
    }

    @JavascriptInterface
    public void setLineNumberAtAddress(int address, int lineNumber) {
        if (address < 0 || address >= 1 << 12) {
            Toast
                    .makeText(mContext,
                            "The JavaScript program tried to write at the address " +
                                    Integer.toHexString(address) + ", which is invalid!",
                            Toast.LENGTH_LONG)
                    .show();
            return;
        }
        if (lineNumber < 0) {
            Toast
                    .makeText(mContext,
                            "The JavaScript program tried to write a line-number " +
                                    lineNumber + ", which is invalid!",
                            Toast.LENGTH_LONG)
                    .show();
            return;
        }
        lineNumbers[address] = lineNumber;
        assembledProgram.lineNumbers[address] = lineNumber;
    }

    @JavascriptInterface
    public void setAssemblyCode(String code) {
        assemblyCode = code;
        assembledProgram.assemblyCode = code;
    }

    @JavascriptInterface
    public String getAssemblyCode() {
        return assembledProgram.assemblyCode;
    }

    @JavascriptInterface
    public void displayCharacterOnTerminal(int characterCode) {
        if (characterCode > 127 || characterCode < 0) {
            Toast
                    .makeText(
                            mContext,
                            "The JavaScript program tried to output a character with a code " +
                                    characterCode +
                                    ", which is not an ASCII character, to the terminal!",
                            Toast.LENGTH_LONG)
                    .show();
            return;
        }
        Log.d("PicoBlaze", "Logging the ASCII character with the code " +
                characterCode + " to the terminal.");
        textDisplayedDuringAssembly += String.copyValueOf(Character.toChars(
                characterCode)); // And I thought appending a character with a specified
        // ASCII code would be as easy in Java as it is in
        // JavaScript. Apparently, it's harder than in both
        // JavaScript and C++.
    }

    @JavascriptInterface
    public void clearTerminal() {
        textDisplayedDuringAssembly = "";
    }

    @JavascriptInterface
    public void flushTheTerminal() {
        assembledProgram.terminalOutputDuringAssembly = textDisplayedDuringAssembly;
        if (!textDisplayedDuringAssembly.isEmpty()) {
            Log.d("PicoBlaze",
                    "During assembly, the program outputted the following:\n" +
                            textDisplayedDuringAssembly);
        }
    }

    @JavascriptInterface
    public void resetDisabledBreakpoints() {
        Log.d("PicoBlaze",
                "Resetting the disabled breakpoints!");
        forbiddenBreakpoints.clear();
    }

    @JavascriptInterface
    public void setDisabledBreakpoint(int address) {
        Log.d("PicoBlaze",
                "Setting it so that a breakpoint cannot be set on the address " + Integer.toHexString(address) + ".");
        forbiddenBreakpoints.add(address);
    }
}
