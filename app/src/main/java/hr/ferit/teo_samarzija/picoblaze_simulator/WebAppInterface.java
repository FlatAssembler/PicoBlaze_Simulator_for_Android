package hr.ferit.teo_samarzija.picoblaze_simulator;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class WebAppInterface {
  public int[] instructions, lineNumbers;
  Context mContext;
  public String assemblyCode;
  GlobalVariables globalVariables;

  WebAppInterface(Context context) {
    mContext=context;
    globalVariables=GlobalVariables.getInstance();
    instructions = globalVariables.instructions;
    lineNumbers = globalVariables.lineNumbers;
    assemblyCode = globalVariables.assemblyCode;
  }

  @JavascriptInterface
  public void setInstructionAtAddress(int address, int instruction) {
    if (address < 0 || address > (1 << 12) - 1) {
      Toast
          .makeText(mContext,
                    "The JavaScript program tried to write at the address " +
                        Integer.toHexString(address) + ", which is invalid!",
                    Toast.LENGTH_LONG)
          .show();
      return;
    }
    if (instruction < 0 || instruction > (1 << 18) - 1) {
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
    globalVariables.instructions[address]=instruction;
  }

  @JavascriptInterface
  public void setLineNumberAtAddress(int address, int lineNumber) {
    if (address < 0 || address > 4096) {
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
    globalVariables.lineNumbers[address]=lineNumber;
  }

  @JavascriptInterface
  public void setAssemblyCode(String code) {
    assemblyCode=code;
    globalVariables.assemblyCode=code;
  }

  @JavascriptInterface public String getAssemblyCode() {
    return globalVariables.assemblyCode;
  }
}
