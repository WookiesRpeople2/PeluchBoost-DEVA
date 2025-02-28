package packages.Json.Parser;

import java.util.ArrayList;
import java.util.List;

public class JsonParser {
    private final String input;
    private int pos = 0;
    private static final char EOF = (char) -1;

    public JsonParser(String input) {
        this.input = input;
    }

    public JsonValue parse() {
        skipWhitespace();
        if (peek() == '{') {
            return new JsonValue(parseObject());
        }else if(peek() == '['){
            return new JsonValue(parseArray());
        }
        throw new IllegalArgumentException("Expected '{' at start of JSON");
    }

    private JsonObject parseObject() {
        JsonObject object = new JsonObject();
        consume('{');
        skipWhitespace();

        while (peek() != '}') {
            if (object.size() > 0) {
                consume(',');
                skipWhitespace();
            }

            String key = parseString();
            skipWhitespace();
            consume(':');
            skipWhitespace();

            JsonValue value = parseValue();
            object.put(key, value);

            skipWhitespace();
        }
        consume('}');
        return object;
    }

    private JsonValue parseValue() {
        char c = peek();
        if (c == '"') {
            return new JsonValue(parseString());
        } else if (c == '{') {
            return new JsonValue(parseObject());
        } else if (c == '[') {
            return new JsonValue(parseArray());
        } else if (c == 't' || c == 'f') {
            return new JsonValue(parseBoolean());
        } else if (c == 'n') {
            return parseNull();
        } else if (isDigit(c) || c == '-') {
            return new JsonValue(parseNumber());
        }
        throw new IllegalArgumentException("Unexpected character in JSON: " + c);
    }

    private String parseString() {
        consume('"');
        StringBuilder sb = new StringBuilder();
        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (c == '"') {
                pos++;
                break;
            }
            if (c == '\\') {
                pos++;
                if (pos >= input.length()) {
                    throw new IllegalArgumentException("Incomplete escape sequence");
                }
                c = input.charAt(pos);
                switch (c) {
                    case '"', '\\', '/' -> sb.append(c);
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    default -> throw new IllegalArgumentException("Invalid escape sequence: \\" + c);
                }
            } else {
                sb.append(c);
            }
            pos++;
        }
        return sb.toString();
    }

    private Number parseNumber() {
        StringBuilder sb = new StringBuilder();
        boolean isDecimal = false;

        if (peek() == '-') {
            sb.append(consume());
        }

        while (isDigit(peek())) {
            sb.append(consume());
        }

        if (peek() == '.') {
            isDecimal = true;
            sb.append(consume());
            if (!isDigit(peek())) {
                throw new IllegalArgumentException("Expected digit after decimal point");
            }
            while (isDigit(peek())) {
                sb.append(consume());
            }
        }

        if (peek() == 'e' || peek() == 'E') {
            isDecimal = true;
            sb.append(consume());
            if (peek() == '+' || peek() == '-') {
                sb.append(consume());
            }
            if (!isDigit(peek())) {
                throw new IllegalArgumentException("Expected digit in exponent");
            }
            while (isDigit(peek())) {
                sb.append(consume());
            }
        }

        String numStr = sb.toString();
        try {
            if (isDecimal) {
                return Double.parseDouble(numStr);
            } else {
                return Long.parseLong(numStr);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format: " + numStr);
        }
    }

    private Boolean parseBoolean() {
        if (peek() == 't') {
            consume("true");
            return true;
        } else {
            consume("false");
            return false;
        }
    }

    private JsonValue parseNull() {
        consume("null");
        return new JsonValue(null);
    }

    private List<JsonValue> parseArray() {
        List<JsonValue> array = new ArrayList<>();
        consume('[');
        skipWhitespace();

        while (peek() != ']') {
            if (!array.isEmpty()) {
                consume(',');
                skipWhitespace();
            }
            array.add(parseValue());
            skipWhitespace();
        }
        consume(']');
        return array;
    }

    private void skipWhitespace() {
        while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
            pos++;
        }
    }

    private char peek() {
        return pos < input.length() ? input.charAt(pos) : EOF;
    }

    private char consume() {
        return pos < input.length() ? input.charAt(pos++) : EOF;
    }

    private void consume(char expected) {
        if (peek() != expected) {
            throw new IllegalArgumentException("Expected '" + expected + "' but found '" + peek() + "'");
        }
        consume();
    }

    private void consume(String expected) {
        for (char c : expected.toCharArray()) {
            consume(c);
        }
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
}
