package br.ufrn.loja.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public final class Json {
    private final String s;
    private int i = 0;

    private Json(String s) { this.s = s; }

    public static class JsonException extends RuntimeException {
        public JsonException(String msg) { super(msg); }
    }

    public static Object parse(String texto) {
        if (texto == null) throw new JsonException("JSON vazio");
        Json p = new Json(texto);
        p.ws();
        Object v = p.valor();
        p.ws();
        if (p.i != p.s.length()) throw new JsonException("Conteúdo extra após o JSON");
        return v;
    }

    private void ws() {
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
    }

    private Object valor() {
        if (i >= s.length()) throw new JsonException("JSON incompleto");
        char c = s.charAt(i);
        switch (c) {
            case '{': return objeto();
            case '[': return array();
            case '"': return string();
            case 't': literal("true"); return Boolean.TRUE;
            case 'f': literal("false"); return Boolean.FALSE;
            case 'n': literal("null"); return null;
            default:
                if (c == '-' || Character.isDigit(c)) return numero();
                throw new JsonException("Caractere inesperado: " + c);
        }
    }

    private void literal(String l) {
        if (!s.startsWith(l, i)) throw new JsonException("Valor inválido");
        i += l.length();
    }

    private Map<String, Object> objeto() {
        Map<String, Object> m = new LinkedHashMap<>();
        i++; ws();
        if (i < s.length() && s.charAt(i) == '}') { i++; return m; }
        while (true) {
            ws();
            if (i >= s.length() || s.charAt(i) != '"') throw new JsonException("Chave esperada");
            String k = string();
            ws();
            if (i >= s.length() || s.charAt(i) != ':') throw new JsonException("':' esperado");
            i++; ws();
            m.put(k, valor());
            ws();
            if (i >= s.length()) throw new JsonException("JSON incompleto");
            char c = s.charAt(i++);
            if (c == '}') return m;
            if (c != ',') throw new JsonException("',' ou '}' esperado");
        }
    }

    private List<Object> array() {
        List<Object> l = new ArrayList<>();
        i++; ws();
        if (i < s.length() && s.charAt(i) == ']') { i++; return l; }
        while (true) {
            ws();
            l.add(valor());
            ws();
            if (i >= s.length()) throw new JsonException("JSON incompleto");
            char c = s.charAt(i++);
            if (c == ']') return l;
            if (c != ',') throw new JsonException("',' ou ']' esperado");
        }
    }

    private String string() {
        StringBuilder sb = new StringBuilder();
        i++;
        while (i < s.length()) {
            char c = s.charAt(i++);
            if (c == '"') return sb.toString();
            if (c == '\\') {
                if (i >= s.length()) break;
                char e = s.charAt(i++);
                switch (e) {
                    case 'n': sb.append('\n'); break;
                    case 't': sb.append('\t'); break;
                    case 'r': sb.append('\r'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'u':
                        if (i + 4 > s.length()) throw new JsonException("Escape unicode inválido");
                        try {
                            sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                        } catch (NumberFormatException ex) {
                            throw new JsonException("Escape unicode inválido");
                        }
                        i += 4;
                        break;
                    default: sb.append(e);
                }
            } else {
                sb.append(c);
            }
        }
        throw new JsonException("String não terminada");
    }

    private BigDecimal numero() {
        int ini = i;
        if (s.charAt(i) == '-') i++;
        while (i < s.length() && "0123456789.eE+-".indexOf(s.charAt(i)) >= 0) i++;
        try {
            return new BigDecimal(s.substring(ini, i));
        } catch (NumberFormatException ex) {
            throw new JsonException("Número inválido");
        }
    }

    public static String stringify(Object o) {
        StringBuilder sb = new StringBuilder();
        escrever(sb, o);
        return sb.toString();
    }

    private static void escrever(StringBuilder sb, Object o) {
        if (o == null) {
            sb.append("null");
        } else if (o instanceof BigDecimal) {
            sb.append(((BigDecimal) o).toPlainString());
        } else if (o instanceof Number || o instanceof Boolean) {
            sb.append(o);
        } else if (o instanceof Map) {
            sb.append('{');
            boolean primeiro = true;
            for (Map.Entry<?, ?> e : ((Map<?, ?>) o).entrySet()) {
                if (!primeiro) sb.append(',');
                primeiro = false;
                escreverString(sb, String.valueOf(e.getKey()));
                sb.append(':');
                escrever(sb, e.getValue());
            }
            sb.append('}');
        } else if (o instanceof List) {
            sb.append('[');
            boolean primeiro = true;
            for (Object x : (List<?>) o) {
                if (!primeiro) sb.append(',');
                primeiro = false;
                escrever(sb, x);
            }
            sb.append(']');
        } else {
            escreverString(sb, o.toString());
        }
    }

    private static void escreverString(StringBuilder sb, String t) {
        sb.append('"');
        for (char c : t.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        sb.append('"');
    }
}
