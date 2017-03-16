package com.example.jason.examplecalculator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import java.text.DecimalFormat;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {
    final static String OPS = "-+*/";
    final static int TEXT_MAX = 12;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // appends a number to the EditText
    protected void onNumPress(Button bt) {
        EditText et = (EditText) findViewById(R.id.editText);
        if (et.getText().toString().length() < TEXT_MAX)
            et.append(bt.getText());
    }

    // appends the operator to the EditText, padded by spaces for the sake of rpn()
    protected void onOpPress(Button bt) {
        EditText et = (EditText) findViewById(R.id.editText);
        if (et.getText().toString().length() < TEXT_MAX)
            et.append(" " + bt.getText() + " ");
    }

    /*
    Called when the equals button is pressed; updates the contents of the EditText field by
    evaluating the equation it represents and displaying the result in its place.
     */
    protected void onEqPress() {
        DecimalFormat df = new DecimalFormat();
        StringBuilder sb = new StringBuilder("0.######E0");

        EditText et = (EditText) findViewById(R.id.editText);
        double result = calculate(rpn(et.getText().toString()));

        String ret = Double.toString(result);
        if (ret.length() > TEXT_MAX) {
            df.applyPattern(sb.toString());
            ret = df.format(result);
            while (ret.length() > TEXT_MAX && sb.toString().matches("0.##*E0")) {
                sb.deleteCharAt(sb.indexOf("#"));
                df.applyPattern(sb.toString());
                ret = df.format(result);
            }
        }
        // if the mantisa somehow makes things too large to display still (don't really think that
        // should happen), display 0 and a toast explaining that the output was too large/precise
        if (ret.length() > TEXT_MAX) {
            ret = "0";
            // display toast
        }
        et.setText(ret);
    }

    /*
    My inexperienced interpretation of a shunting yard algorithm. Pushes operands to the end of a
    string, pushes operators to the operator stack until they can be pushed to the end of the
    string. Operator precedence is determined by the order of operators in the ops String.
     */
    protected String rpn(String eq) {
        StringBuilder sb = new StringBuilder(); // constructs string representing the rpn conversion
        // operator indices are stored here
        // due to the nature of the ops list having two operators each of two total precedences,
        // dividing its index by two will give an operator's precedence value
        Stack<Integer> stk = new Stack<>();

        for (String str : eq.split("\\s")) {
            char c = str.charAt(0);
            int index = OPS.indexOf(c);

            // check if c is an operator
            if (index != -1) {
                if (str.isEmpty()) stk.push(index);
                else {
                    while (!str.isEmpty()) {
                        int next = stk.peek() / 2;
                        int cur = index / 2;
                        // if current op has a greater precedence than the top of the stack,
                        // pop the stack until it does not
                        if (next > cur || next == cur)
                            sb.append(OPS.charAt(stk.pop())).append(' ');
                        else break;
                    }
                    // only push to the operator stack when the operator in question
                    // has the least (or equal to least) precedence on stack
                    stk.push(index);
                }
            }
            else if (c == '(')
                stk.push(-2);   // arbitrary value representing (
            else if (c == ')') {
                // pop operators until -2 (representing '(') is reached
                while (stk.peek() != -2)
                    sb.append(OPS.charAt(stk.pop())).append(' ');
                stk.pop();  // discard the (
            }
            else
                sb.append(str).append(' ');
        }
        while (!stk.isEmpty())
            sb.append(OPS.charAt(stk.pop())).append(' ');

        return sb.toString();
    }

    /*
    Produces the result (as a string) of parsing and evaluating an equation represented in reverse
    polish notation. Results too big to display will be represented in scientific notation.
     */
    protected double calculate(String rpn) {
        return 0.0;
    }
}