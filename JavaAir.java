import java.io.*;
import java.util.*;

public class JavaAir {

    static Map<String, Object> variables = new HashMap<>();

    static Map<String, Map<String, Object>> objects = new HashMap<>();
    public static void main(String[] args) throws Exception {

        File file = new File("program.air");
        BufferedReader br = new BufferedReader(new FileReader(file));

        String line;

        while ((line = br.readLine()) != null) {
            execute(line.trim());
        }

        br.close();
    }

    static void execute(String line) {
        line = sanitizeLine(line);
        if (line.isEmpty()) {
            return;
        }

        if (line.startsWith("if")) {
            handleIf(line);
            return;
        }

        if (line.startsWith("print")) {
            handlePrint(line);
            return;
        }

        if (line.startsWith("set")) {
            handleSet(line);
            return;
        }

        if (line.startsWith("add")) {
            handleAdd(line);
            return;
        }

        if (line.startsWith("repeat")) {
            handleRepeat(line);
            return;
        }

        if (line.startsWith("object")) {
            handleObject(line);
            return;
        }

        System.out.println("❌ Unknown command: " + line);
    }

    static String sanitizeLine(String line) {
        if (line == null) {
            return "";
        }

        line = line.trim();

        while (line.endsWith(";")) {
            line = line.substring(0, line.length() - 1).trim();
        }

        return line;
    }

    static void handlePrint(String line) {
        String payload = line.substring("print".length()).trim();

        if (payload.isEmpty()) {
            System.out.println();
            return;
        }

        String expression;
        if (payload.startsWith("(") && payload.endsWith(")")) {
            expression = payload.substring(1, payload.length() - 1).trim();
        } else {
            expression = payload;
        }

        Object value = resolveValue(expression);
        if (value == null) {
            System.out.println("null");
        } else {
            System.out.println(value);
        }
    }

    static void handleSet(String line) {
        String payload = line.substring("set".length()).trim();
        int eqIndex = payload.indexOf('=');

        if (eqIndex < 1) {
            System.out.println("❌ Invalid set syntax");
            return;
        }

        String target = payload.substring(0, eqIndex).trim();
        String rawValue = payload.substring(eqIndex + 1).trim();

        if (target.isEmpty()) {
            System.out.println("❌ Invalid set target");
            return;
        }

        Object valueToStore;
        if (rawValue.startsWith("int(") && rawValue.endsWith(")")) {
            String innerExpr = rawValue.substring(4, rawValue.length() - 1).trim();
            valueToStore = evaluateIntExpression(innerExpr);
        } else if (isQuoted(rawValue)) {
            valueToStore = unquote(rawValue);
        } else if (variables.containsKey(rawValue)) {
            valueToStore = variables.get(rawValue);
        } else {
            valueToStore = rawValue;
        }

        assignTarget(target, valueToStore);
    }

    static void assignTarget(String target, Object value) {
        if (target.contains(".")) {
            String[] parts = target.split("\\.", 2);
            String objName = parts[0].trim();
            String prop = parts[1].trim();

            Map<String, Object> objectMap = objects.get(objName);
            if (objectMap == null) {
                objectMap = new HashMap<>();
                objects.put(objName, objectMap);
            }

            objectMap.put(prop, value);
            return;
        }

        variables.put(target, value);
    }

    static void handleAdd(String line) {
        String[] parts = line.split("\\s+");
        if (parts.length < 3) {
            System.out.println("❌ Invalid add syntax");
            return;
        }

        String varName = parts[1];
        int delta = evaluateIntExpression(parts[2]);
        int current = toInt(variables.get(varName));
        variables.put(varName, current + delta);
    }

    static void handleIf(String line) {
        int openParen = line.indexOf('(');
        int closeParen = findMatchingBracket(line, openParen, '(', ')');

        if (openParen < 0 || closeParen < 0) {
            String[] parts = line.split("\\s+");
            if (parts.length < 5) {
                System.out.println("❌ Invalid if syntax");
                return;
            }

            String left = parts[1];
            String op = parts[2];
            String right = parts[3];
            String trueCommand = String.join(" ", Arrays.copyOfRange(parts, 4, parts.length));
            if (evaluateComparison(left, op, right)) {
                execute(trueCommand);
            }
            return;
        }

        String conditionText = line.substring(openParen + 1, closeParen).trim();
        String remainder = line.substring(closeParen + 1).trim();

        String trueCommand;
        String falseCommand = null;

        if (remainder.startsWith("{")) {
            int trueClose = findMatchingBracket(remainder, 0, '{', '}');
            if (trueClose < 0) {
                System.out.println("❌ Missing closing } in if block");
                return;
            }
            trueCommand = remainder.substring(1, trueClose).trim();
            remainder = remainder.substring(trueClose + 1).trim();
        } else {
            int elseIndex = findElseIndex(remainder);
            if (elseIndex >= 0) {
                trueCommand = remainder.substring(0, elseIndex).trim();
                remainder = remainder.substring(elseIndex).trim();
            } else {
                trueCommand = remainder;
                remainder = "";
            }
        }

        if (remainder.startsWith("else")) {
            String afterElse = remainder.substring(4).trim();
            if (afterElse.startsWith("{")) {
                int falseClose = findMatchingBracket(afterElse, 0, '{', '}');
                if (falseClose < 0) {
                    System.out.println("❌ Missing closing } in else block");
                    return;
                }
                falseCommand = afterElse.substring(1, falseClose).trim();
            } else {
                falseCommand = afterElse;
            }
        }

        if (evaluateCondition(conditionText)) {
            if (!trueCommand.isEmpty()) {
                execute(trueCommand);
            }
        } else if (falseCommand != null && !falseCommand.isEmpty()) {
            execute(falseCommand);
        }
    }

    static int findElseIndex(String text) {
        int depthParen = 0;
        int depthBrace = 0;
        for (int i = 0; i < text.length() - 3; i++) {
            char c = text.charAt(i);
            if (c == '(') depthParen++;
            if (c == ')') depthParen--;
            if (c == '{') depthBrace++;
            if (c == '}') depthBrace--;

            if (depthParen == 0 && depthBrace == 0) {
                String probe = text.substring(i).toLowerCase(Locale.ROOT);
                if (probe.startsWith("else") && (i == 0 || Character.isWhitespace(text.charAt(i - 1)))) {
                    return i;
                }
            }
        }
        return -1;
    }

    static boolean evaluateCondition(String conditionText) {
        List<String> orParts = splitByTopLevel(conditionText, "||", "or");
        boolean orResult = false;

        for (String orPart : orParts) {
            List<String> andParts = splitByTopLevel(orPart, "&&", "and");
            boolean andResult = true;
            for (String andPart : andParts) {
                andResult = andResult && evaluateAtomicCondition(andPart.trim());
            }
            orResult = orResult || andResult;
        }

        return orResult;
    }

    static boolean evaluateAtomicCondition(String text) {
        String[] operators = {">=", "<=", "==", "!=", ">", "<"};
        for (String op : operators) {
            int idx = text.indexOf(op);
            if (idx > 0) {
                String left = text.substring(0, idx).trim();
                String right = text.substring(idx + op.length()).trim();
                return evaluateComparison(left, op, right);
            }
        }

        Object value = resolveValue(text);
        if (value instanceof Integer) {
            return ((Integer) value) != 0;
        }
        return value != null && !value.toString().isEmpty();
    }

    static boolean evaluateComparison(String leftExpr, String operator, String rightExpr) {
        Object left = resolveValue(leftExpr);
        Object right = resolveValue(rightExpr);

        if (isNumericLike(left) && isNumericLike(right)) {
            int a = toInt(left);
            int b = toInt(right);
            switch (operator) {
                case ">": return a > b;
                case "<": return a < b;
                case ">=": return a >= b;
                case "<=": return a <= b;
                case "==": return a == b;
                case "!=": return a != b;
                default: return false;
            }
        }

        String a = left == null ? "null" : left.toString();
        String b = right == null ? "null" : right.toString();
        int cmp = a.compareTo(b);

        switch (operator) {
            case "==": return a.equals(b);
            case "!=": return !a.equals(b);
            case ">": return cmp > 0;
            case "<": return cmp < 0;
            case ">=": return cmp >= 0;
            case "<=": return cmp <= 0;
            default: return false;
        }
    }

    static List<String> splitByTopLevel(String text, String symbol, String word) {
        List<String> out = new ArrayList<>();
        int depthParen = 0;
        int depthBrace = 0;
        int start = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '(') depthParen++;
            if (c == ')') depthParen--;
            if (c == '{') depthBrace++;
            if (c == '}') depthBrace--;

            if (depthParen == 0 && depthBrace == 0) {
                if (i + symbol.length() <= text.length() && text.substring(i, i + symbol.length()).equals(symbol)) {
                    out.add(text.substring(start, i).trim());
                    start = i + symbol.length();
                    i = start - 1;
                    continue;
                }

                if (matchesWordAt(text, i, word)) {
                    out.add(text.substring(start, i).trim());
                    start = i + word.length();
                    i = start - 1;
                }
            }
        }

        out.add(text.substring(start).trim());
        return out;
    }

    static boolean matchesWordAt(String text, int index, String word) {
        if (index + word.length() > text.length()) {
            return false;
        }

        String sub = text.substring(index, index + word.length());
        if (!sub.equalsIgnoreCase(word)) {
            return false;
        }

        boolean leftBoundary = index == 0 || Character.isWhitespace(text.charAt(index - 1));
        boolean rightBoundary = index + word.length() == text.length()
                || Character.isWhitespace(text.charAt(index + word.length()));

        return leftBoundary && rightBoundary;
    }

    static int findMatchingBracket(String text, int openIndex, char open, char close) {
        if (openIndex < 0 || openIndex >= text.length() || text.charAt(openIndex) != open) {
            return -1;
        }

        int depth = 0;
        for (int i = openIndex; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == open) depth++;
            if (c == close) depth--;
            if (depth == 0) return i;
        }
        return -1;
    }

    static Object resolveValue(String expr) {
        expr = expr.trim();

        if (expr.isEmpty()) {
            return "";
        }

        if (expr.startsWith("int(") && expr.endsWith(")")) {
            return evaluateIntExpression(expr.substring(4, expr.length() - 1));
        }

        if (isQuoted(expr)) {
            return unquote(expr);
        }

        if (expr.contains(".")) {
            String[] parts = expr.split("\\.", 2);
            Map<String, Object> obj = objects.get(parts[0]);
            if (obj != null && obj.containsKey(parts[1])) {
                return obj.get(parts[1]);
            }
        }

        if (variables.containsKey(expr)) {
            return variables.get(expr);
        }

        String normalized = expr.replaceAll("[\\.,!?]+$", "");
        if (!normalized.equals(expr) && variables.containsKey(normalized)) {
            return variables.get(normalized);
        }

        if (expr.matches("[-+]?\\d+")) {
            return Integer.parseInt(expr);
        }

        if (looksLikeArithmetic(expr)) {
            try {
                return evaluateIntExpression(expr);
            } catch (RuntimeException ignored) {
            }
        }

        return expr;
    }

    static boolean looksLikeArithmetic(String expr) {
        return expr.matches(".*[+\\-*/%()].*");
    }

    static int evaluateIntExpression(String expr) {
        return new IntExpressionParser(expr).parse();
    }

    static boolean isQuoted(String text) {
        return (text.startsWith("\"") && text.endsWith("\""))
                || (text.startsWith("'") && text.endsWith("'"));
    }

    static String unquote(String text) {
        if (text.length() >= 2) {
            return text.substring(1, text.length() - 1);
        }
        return text;
    }

    static boolean isNumericLike(Object value) {
        if (value instanceof Integer) {
            return true;
        }
        return value != null && value.toString().matches("[-+]?\\d+");
    }

    static int toInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        String text = value.toString().trim();
        if (text.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(text);
    }

    static void handleRepeat(String line) {
        String[] parts = line.split("\\s+", 3);
        if (parts.length < 3) {
            System.out.println("❌ Invalid repeat syntax");
            return;
        }

        int times = evaluateIntExpression(parts[1]);
        String command = parts[2].trim();

        for (int i = 0; i < times; i++) {
            execute(command);
        }
    }

    static void handleObject(String line) {
        String[] parts = line.split("\\s+");
        if (parts.length < 2) {
            System.out.println("❌ Invalid object syntax");
            return;
        }
        objects.put(parts[1], new HashMap<>());
    }

    static class IntExpressionParser {
        private final String input;
        private int index;

        IntExpressionParser(String input) {
            this.input = input;
            this.index = 0;
        }

        int parse() {
            int value = parseExpression();
            skipSpaces();
            if (index != input.length()) {
                throw new RuntimeException("Unexpected token in expression: " + input.substring(index));
            }
            return value;
        }

        private int parseExpression() {
            int value = parseTerm();
            while (true) {
                skipSpaces();
                if (match('+')) {
                    value += parseTerm();
                } else if (match('-')) {
                    value -= parseTerm();
                } else {
                    break;
                }
            }
            return value;
        }

        private int parseTerm() {
            int value = parseFactor();
            while (true) {
                skipSpaces();
                if (match('*')) {
                    value *= parseFactor();
                } else if (match('/')) {
                    int rhs = parseFactor();
                    if (rhs == 0) {
                        throw new RuntimeException("Division by zero");
                    }
                    value /= rhs;
                } else if (match('%')) {
                    int rhs = parseFactor();
                    if (rhs == 0) {
                        throw new RuntimeException("Modulo by zero");
                    }
                    value %= rhs;
                } else {
                    break;
                }
            }
            return value;
        }

        private int parseFactor() {
            skipSpaces();

            if (match('+')) {
                return parseFactor();
            }
            if (match('-')) {
                return -parseFactor();
            }

            if (match('(')) {
                int value = parseExpression();
                skipSpaces();
                if (!match(')')) {
                    throw new RuntimeException("Missing closing parenthesis");
                }
                return value;
            }

            if (index >= input.length()) {
                throw new RuntimeException("Unexpected end of expression");
            }

            char c = input.charAt(index);
            if (Character.isDigit(c)) {
                return parseNumber();
            }

            if (Character.isLetter(c) || c == '_') {
                String identifier = parseIdentifier();
                Object value = resolveValue(identifier);
                if (!isNumericLike(value)) {
                    throw new RuntimeException("Non-numeric value in int expression: " + identifier);
                }
                return toInt(value);
            }

            throw new RuntimeException("Unexpected character in expression: " + c);
        }

        private int parseNumber() {
            int start = index;
            while (index < input.length() && Character.isDigit(input.charAt(index))) {
                index++;
            }
            return Integer.parseInt(input.substring(start, index));
        }

        private String parseIdentifier() {
            int start = index;
            while (index < input.length()) {
                char c = input.charAt(index);
                if (Character.isLetterOrDigit(c) || c == '_' || c == '.') {
                    index++;
                } else {
                    break;
                }
            }
            return input.substring(start, index);
        }

        private void skipSpaces() {
            while (index < input.length() && Character.isWhitespace(input.charAt(index))) {
                index++;
            }
        }

        private boolean match(char expected) {
            if (index < input.length() && input.charAt(index) == expected) {
                index++;
                return true;
            }
            return false;
        }
    }
}
