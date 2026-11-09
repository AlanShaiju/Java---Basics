private static String combine(String controllerPath, String methodPath) {
    String c = controllerPath == null ? "" : controllerPath.trim();
    String m = methodPath == null ? "" : methodPath.trim();
    if (c.isEmpty() && m.isEmpty()) {
        return "/";
    }
    String normalizedC = c.endsWith("/") ? c.substring(0, c.length() - 1) : c;
    String normalizedM = m.startsWith("/") ? m.substring(1) : m;
    String joined = normalizedC.isEmpty() ? "/" + normalizedM : normalizedC + "/" + normalizedM;
    return joined.startsWith("/") ? joined : "/" + joined;
}
