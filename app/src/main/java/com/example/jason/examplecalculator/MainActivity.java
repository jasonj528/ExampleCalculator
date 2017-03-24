package com.example.jason.examplecalculator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {
    // if the shunting yard algorithm needs to be extended at some point, add new functions
    // to the OPS variable or change it to a String array
    final static String OPS = "-+x/";
    // the size of the return string at which scientific notation will be used
    final static int RESULT_THRESHOLD = 12;

    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = (TextView) findViewById(R.id.result);
    }

    /*
    In the future maybe combine onNumPress and onOpPress and check the button against OPS. It's
    definitely redundant that I'm checking twice for whether the text box is 0 or not
     */

    // appends a number to the EditText
    public void onNumPress(View view) {
        String str = tv.getText().toString();
        String ret = ((Button) view).getText().toString();

        // don't add more than one decimal point
        if (ret.equals(getString(R.string.decimal)) && str.charAt(str.length() - 1) == '.');
        // if the TextView is cleared (0) substitute the zero for the input number
        else if (str.equals(getString(R.string.zero)) && !ret.equals(getString(R.string.decimal)))
            tv.setText(ret);
        else
            tv.append(ret);
    }

    // appends the operator to the EditText, padded by spaces for the sake of rpn()
    public void onOpPress(View view) {
        String str = tv.getText().toString();
        String ret = ((Button) view).getText().toString();

        // if the TextView is cleared (0) or an operation was last used and - was pressed,
        // simply append the - (that is, a negative number is being input.
        if (str.equals(getString(R.string.zero)) || str.charAt(str.length() - 1) == ' ' ) {
            if (str.equals(getString(R.string.subtract))) {
                onNumPress(view);
                return;
            }
            // don't respond to an operator following another operator
            else return;
        }
        tv.append(" " + ret + " ");
    }

    /*
    Called when the equals button is pressed; updates the contents of the EditText field by
    evaluating the equation it represents and displaying the result in its place.
     */
    public void onEqPress(View view) {

        // DecimalFormat df = new DecimalFormat("0.######E0");

        String text = tv.getText().toString();
        // if (text.matches("\\d+\\.\\d+")) return; // if there's just a number and no ops, return
        // if there's a space at the end of the string, an operation is used without sufficient
        // operands (in this case < 2)
        if (text.charAt(text.length() - 1) == ' ' || text.charAt(text.length() - 1) == '.') {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.eqErr), Toast.LENGTH_SHORT).show();
            return;
        }
        double result = calculate(rpn(tv.getText().toString()));

        // if (Double.toString(result).length() > RESULT_THRESHOLD)
        // tv.setText(df.format(result));
        tv.setText(Double.toString(result));
    }

    // Called when the clear button is pressed; clears the results text box
    public void onClrPress(View view) {
        tv.setText(getString(R.string.zero));
    }

    // Called when the backspace button is pressed, removes the last character or space-padded op
    public void onBkspPress(View view) {
        String str = tv.getText().toString();
        if (!str.equals(getString(R.string.zero))) {
            if (str.length() == 1)
                tv.setText(getString(R.string.zero));
            else if (str.charAt(str.length() - 1) == ' ')
                tv.setText(str.substring(0, str.length()-3));
            else
                tv.setText(str.substring(0, str.length() - 1));
        }
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

            // if the operation is - but the string is longer than a character,
            // it's a negative number
            if (index == 0 && str.length() > 1) index = -1;

            // check if c is an operator
            if (index != -1) {
                if (stk.isEmpty()) stk.push(index);
                else {
                    while (!stk.isEmpty()) {
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
            /*
            Uncomment if parentheses are ever supported. This will require handling the negative
            sign interpreted differently from subtraction (ie declare negative a different symbol
            from -, for example n12 for -12 such that n(3 + 4) = n7)
            else if (c == '(')
                stk.push(-2);   // arbitrary value representing (
            else if (c == ')') {
                // pop operators until -2 (representing '(') is reached
                while (stk.peek() != -2)
                    sb.append(OPS.charAt(stk.pop())).append(' ');
                stk.pop();  // discard the (
            }
             */
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
        // for now, only binary operations are implemented, so we'll keep track only of right/left
        Stack<Double> operands = new Stack<>();

        for (String str : rpn.split("\\s")) {
            if (str.matches("-?[0-9]+\\.?[0-9]*"))
                operands.push(Double.parseDouble(str));
            else {
                // add conditional here for what number of operands a group of ops take
                double rhs = operands.pop();
                double lhs = operands.pop();
                if (str.equals(getString(R.string.subtract)))
                    operands.push(lhs - rhs);
                else if (str.equals(getString(R.string.add)))
                    operands.push(lhs + rhs);
                else if (str.equals(getString(R.string.multiply)))
                    operands.push(lhs * rhs);
                else if (str.equals(getString(R.string.divide)))
                    operands.push(lhs / rhs);
                // add additional operations here
            }
        }

        return operands.pop();
    }
}