package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.util.ArrayList;
import java.util.Locale;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    EditText tvDisplay;
    TextView tvExpression;

    String expression = "";
    boolean justCalculated = false;

    ArrayList<String> history = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // =========================================================
        // FULLSCREEN / IMMERSIVE MODE
        // =========================================================
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat windowInsetsController =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        setContentView(R.layout.activity_main);

        tvDisplay = findViewById(R.id.tvDisplay);
        tvExpression = findViewById(R.id.tvExpression);

        tvDisplay.setText("0");
        tvDisplay.setCursorVisible(true);
        tvDisplay.setShowSoftInputOnFocus(false);

        setupNumberButtons();
        setupOperators();
        setupFunctions();
    }

    private void setupNumberButtons() {
        int[] buttons = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        };

        View.OnClickListener listener = view -> {
            androidx.appcompat.widget.AppCompatButton button =
                    (androidx.appcompat.widget.AppCompatButton) view;

            String number = button.getText().toString();

            if (justCalculated) {
                expression = "";
                tvExpression.setText("");
                justCalculated = false;
            }

            if (expression.equals("0")) {
                expression = number;
            } else {
                expression += number;
            }
            updateDisplay();
        };

        for (int id : buttons) {
            findViewById(id).setOnClickListener(listener);
        }
    }

    private void setupOperators() {
        findViewById(R.id.btnPlus).setOnClickListener(v -> addOperator("+"));
        findViewById(R.id.btnMinus).setOnClickListener(v -> addOperator("-"));
        findViewById(R.id.btnMultiply).setOnClickListener(v -> addOperator("*"));
        findViewById(R.id.btnDivide).setOnClickListener(v -> addOperator("/"));
    }

    private void addOperator(String operator) {
        if (expression.isEmpty()) return;

        if (isLastCharacterOperator()) {
            expression = expression.substring(0, expression.length() - 1) + operator;
        } else {
            expression += operator;
        }

        justCalculated = false;
        updateDisplay();
    }

    private boolean isLastCharacterOperator() {
        if (expression.isEmpty()) return false;
        char c = expression.charAt(expression.length() - 1);
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private void setupFunctions() {
        findViewById(R.id.btnClear).setOnClickListener(v -> {
            expression = "";
            tvExpression.setText("");
            tvDisplay.setText("0");
            justCalculated = false;
        });

        findViewById(R.id.btnBackspace).setOnClickListener(v -> {
            if (expression.length() > 0) {
                expression = expression.substring(0, expression.length() - 1);
                updateDisplay();
            }
            if (expression.isEmpty()) {
                tvDisplay.setText("0");
            }
        });

        findViewById(R.id.btnDot).setOnClickListener(v -> addDecimal());
        findViewById(R.id.btnPercent).setOnClickListener(v -> addPercent());
        findViewById(R.id.btnBracket).setOnClickListener(v -> addBracket());
        findViewById(R.id.btnEqual).setOnClickListener(v -> calculate());
        findViewById(R.id.btnHistory).setOnClickListener(v -> showHistory());
    }

    private void addDecimal() {
        if (justCalculated) {
            expression = "";
            tvExpression.setText("");
            justCalculated = false;
        }

        String currentNumber = getCurrentNumber();
        if (!currentNumber.contains(".")) {
            if (currentNumber.isEmpty()) {
                expression += "0.";
            } else {
                expression += ".";
            }
            updateDisplay();
        }
    }

    private void addPercent() {
        if (expression.isEmpty()) return;
        if (isLastCharacterOperator()) return;

        if (!expression.endsWith("%")) {
            expression += "%";
            updateDisplay();
        }
    }

    private void addBracket() {
        if (expression.isEmpty()) {
            expression = "(";
        } else {
            int open = countCharacter('(');
            int close = countCharacter(')');
            char last = expression.charAt(expression.length() - 1);

            if (Character.isDigit(last) || last == ')' || last == '%') {
                if (open > close) {
                    expression += ")";
                } else {
                    expression += "*(";
                }
            } else {
                expression += "(";
            }
        }
        updateDisplay();
    }

    private int countCharacter(char character) {
        int count = 0;
        for (char c : expression.toCharArray()) {
            if (c == character) count++;
        }
        return count;
    }

    private void calculate() {
        if (expression.isEmpty()) return;

        try {
            String originalExpression = expression;
            double result = evaluate(expression);
            String answer = formatResult(result);

            tvExpression.setText(
                    originalExpression.replace("*", "×").replace("/", "÷") + " ="
            );
            tvDisplay.setText(answer);

            history.add(
                    originalExpression.replace("*", "×").replace("/", "÷") + " = " + answer
            );

            expression = answer;
            justCalculated = true;

        } catch (Exception e) {
            tvExpression.setText(expression);
            tvDisplay.setText("Error");
            expression = "";
        }
    }

    private void updateDisplay() {
        String visible = expression.replace("*", "×").replace("/", "÷");
        tvDisplay.setText(visible.isEmpty() ? "0" : visible);
        tvDisplay.setSelection(tvDisplay.length());
    }

    private String getCurrentNumber() {
        if (expression.isEmpty()) return "";
        int start = expression.length() - 1;
        while (start >= 0) {
            char c = expression.charAt(start);
            if (c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == ')') {
                break;
            }
            start--;
        }
        return expression.substring(start + 1);
    }

    private String formatResult(double result) {
        if (Double.isInfinite(result) || Double.isNaN(result)) {
            return "Error";
        }
        if (result == (long) result) {
            return String.valueOf((long) result);
        }
        return String.format(Locale.US, "%.10f", result)
                .replaceAll("0+$", "")
                .replaceAll("\\.$", "");
    }

    private double evaluate(String input) {
        Parser parser = new Parser(input);
        double result = parser.parseExpression();
        if (parser.position < input.length()) {
            throw new RuntimeException("Invalid expression");
        }
        return result;
    }

    private class Parser {
        String input;
        int position = 0;

        Parser(String input) {
            this.input = input;
        }

        private class ParseResult {
            double val;
            boolean hasPercent;

            ParseResult(double val, boolean hasPercent) {
                this.val = val;
                this.hasPercent = hasPercent;
            }
        }

        double parseExpression() {
            double value = parseTerm();
            while (position < input.length()) {
                char operator = input.charAt(position);
                if (operator != '+' && operator != '-') break;
                position++;

                ParseResult right = parseTermWithPercentCheck();
                if (right.hasPercent) {
                    double percentVal = (value * right.val) / 100.0;
                    if (operator == '+') value += percentVal;
                    else value -= percentVal;
                } else {
                    if (operator == '+') value += right.val;
                    else value -= right.val;
                }
            }
            return value;
        }

        double parseTerm() {
            ParseResult res = parseTermWithPercentCheck();
            return res.hasPercent ? res.val / 100.0 : res.val;
        }

        ParseResult parseTermWithPercentCheck() {
            ParseResult left = parseFactorWithPercentCheck();
            while (position < input.length()) {
                char operator = input.charAt(position);
                if (operator != '*' && operator != '/') break;
                position++;

                ParseResult right = parseFactorWithPercentCheck();
                double leftVal = left.hasPercent ? left.val / 100.0 : left.val;
                double rightVal = right.hasPercent ? right.val / 100.0 : right.val;

                if (operator == '*') {
                    leftVal *= rightVal;
                } else {
                    if (rightVal == 0) throw new ArithmeticException("Divide by zero");
                    leftVal /= rightVal;
                }
                left = new ParseResult(leftVal, false);
            }
            return left;
        }

        ParseResult parseFactorWithPercentCheck() {
            skipSpaces();
            if (position >= input.length()) throw new RuntimeException("Unexpected end");

            char c = input.charAt(position);
            double value = 0;
            boolean isPercent = false;

            if (c == '+' || c == '-') {
                position++;
                ParseResult res = parseFactorWithPercentCheck();
                value = c == '-' ? -res.val : res.val;
                isPercent = res.hasPercent;
                return new ParseResult(value, isPercent);
            }

            if (c == '(') {
                position++;
                value = parseExpression();
                if (position >= input.length() || input.charAt(position) != ')') {
                    throw new RuntimeException("Missing closing bracket");
                }
                position++;
            } else if ((c >= '0' && c <= '9') || c == '.') {
                int start = position;
                while (position < input.length()) {
                    char ch = input.charAt(position);
                    if ((ch >= '0' && ch <= '9') || ch == '.') position++;
                    else break;
                }
                value = Double.parseDouble(input.substring(start, position));
            } else {
                throw new RuntimeException("Unexpected character");
            }

            skipSpaces();
            if (position < input.length() && input.charAt(position) == '%') {
                position++;
                isPercent = true;
            }
            return new ParseResult(value, isPercent);
        }

        void skipSpaces() {
            while (position < input.length() && input.charAt(position) == ' ') position++;
        }
    }
    // =========================================================
    // HISTORY & SHARE (SLIDING BOTTOM SHEET)
    // =========================================================
    private void showHistory() {
        // Bottom Sheet ডায়ালগ তৈরি
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_history, null);
        bottomSheetDialog.setContentView(view);

        // ভিউগুলো কানেক্ট করা
        android.widget.LinearLayout historyContainer = view.findViewById(R.id.historyContainer);
        TextView tvEmptyHistory = view.findViewById(R.id.tvEmptyHistory);
        TextView btnClear = view.findViewById(R.id.btnClearHistory);
        TextView btnShare = view.findViewById(R.id.btnShareHistory);
        TextView btnClose = view.findViewById(R.id.btnCloseHistory);

        // হিস্ট্রি চেক করা
        if (history.isEmpty()) {
            tvEmptyHistory.setVisibility(View.VISIBLE);
            historyContainer.setVisibility(View.GONE);
        } else {
            tvEmptyHistory.setVisibility(View.GONE);
            historyContainer.setVisibility(View.VISIBLE);

            // লুপ চালিয়ে হিস্ট্রির ডেটাগুলো এড করা
            for (String item : history) {
                TextView tv = new TextView(this);
                tv.setText(item);
                tv.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                tv.setTextSize(18f);
                tv.setPadding(0, 16, 0, 16); // প্রতিটি লাইনের মাঝে গ্যাপ
                historyContainer.addView(tv);
            }
        }

        // বাটনগুলোর কাজ
        btnClear.setOnClickListener(v -> {
            history.clear();
            bottomSheetDialog.dismiss();
        });

        btnShare.setOnClickListener(v -> {
            shareHistoryData();
        });

        btnClose.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
        });

        // ডায়ালগ শো করা
        bottomSheetDialog.show();
        bottomSheetDialog.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
        bottomSheetDialog.getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

    }
    private void shareHistoryData() {
        if (history.isEmpty()) return;

        StringBuilder shareBody = new StringBuilder("My Calculation History:\n\n");
        for (String item : history) {
            shareBody.append(item).append("\n");
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Calculator History");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody.toString());
        startActivity(Intent.createChooser(shareIntent, "Share History via"));
    }
}