package com.example.mathassistant.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class MathematicalAssistantController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("selectedOption", "math");
        return "index";
    }

    @PostMapping("/calculate")
    public String calculate(@RequestParam(value = "num1", required = false) String num1Str,
                            @RequestParam(value = "num2", required = false) String num2Str,
                            @RequestParam(value = "operation", required = false) String operation,
                            @RequestParam(value = "option", required = false, defaultValue = "math") String option,
                            Model model) {
        String result = "";
        String validationError = "";

        if (num1Str == null || num1Str.trim().isEmpty() || num2Str == null || num2Str.trim().isEmpty()) {
            validationError = "Please enter both numbers.";
        } else if (operation == null || operation.trim().isEmpty()) {
            validationError = "Please select an operation.";
        } else {
            try {
                double num1 = Double.parseDouble(num1Str.trim());
                double num2 = Double.parseDouble(num2Str.trim());
                double res = 0;
                switch (operation.trim()) {
                    case "+": res = num1 + num2; break;
                    case "-": res = num1 - num2; break;
                    case "*": res = num1 * num2; break;
                    case "/":
                        if (num2 == 0) throw new ArithmeticException("Division by zero");
                        res = num1 / num2; break;
                    default: validationError = "Invalid operation."; break;
                }
                if (validationError.isEmpty()) {
                    result = "Result: " + res;
                }
            } catch (NumberFormatException e) {
                validationError = "Invalid number format.";
            } catch (ArithmeticException e) {
                validationError = "Error: Division by zero.";
            }
        }

        model.addAttribute("result", result);
        model.addAttribute("validationError", validationError);
        model.addAttribute("selectedOption", option);
        model.addAttribute("operation", operation);
        return "index";
    }

    @PostMapping("/fibonacci-curve")
    public String drawFibCurve(@RequestParam(value = "n", required = false) String nStr,
                               @RequestParam(value = "option", required = false, defaultValue = "fib") String option,
                               Model model) {
        String svg = "";
        String error = "";
        int n = 0;

        try {
            if (nStr == null || nStr.trim().isEmpty()) {
                error = "Please enter n.";
            } else {
                n = Integer.parseInt(nStr.trim());
                if (n < 1) {
                    error = "n must be at least 1.";
                } else if (n > 50) {
                    error = "n too large (max 50).";
                } else {
                    // Generate Fibonacci sequence
                    List<Double> fibs = new ArrayList<>();
                    if (n >= 1) fibs.add(1.0);
                    if (n >= 2) fibs.add(1.0);
                    for (int i = 2; i < n; i++) {
                        fibs.add(fibs.get(i - 1) + fibs.get(i - 2));
                    }

                    // Generate arcs and compute bounds
                    double currX = 0, currY = 0, theta_deg = 0;
                    StringBuilder path = new StringBuilder("M 0 0 ");
                    double minX = 0, maxX = 0, minY = 0, maxY = 0;

                    for (double r : fibs) {
                        double sin_th = Math.sin(Math.toRadians(theta_deg));
                        double cos_th = Math.cos(Math.toRadians(theta_deg));
                        double px = -sin_th, py = cos_th;
                        double cenX = currX + r * px;
                        double cenY = currY + r * py;

                        double vx = currX - cenX;
                        double vy = currY - cenY;
                        double start_ang = Math.toDegrees(Math.atan2(vy, vx));
                        double end_ang = start_ang + 90;

                        double rx = r, ry = r, xAxisRotation = 0;
                        double large = 0, sweep = 1;
                        double endX = cenX + r * Math.cos(Math.toRadians(end_ang));
                        double endY = cenY + r * Math.sin(Math.toRadians(end_ang));

                        path.append(String.format("A %.3f %.3f %.0f %.0f %.0f %.3f %.3f ",
                                rx, ry, xAxisRotation, large, sweep, endX, endY));

                        currX = endX; currY = endY;
                        theta_deg = (theta_deg + 90) % 360;

                        minX = Math.min(minX, Math.min(cenX - r, currX));
                        maxX = Math.max(maxX, Math.max(cenX + r, currX));
                        minY = Math.min(minY, Math.min(cenY - r, currY));
                        maxY = Math.max(maxY, Math.max(cenY + r, currY));
                    }

                    // Safe scaling and SVG build
                    double worldW = Math.max(1e-6, maxX - minX);
                    double worldH = Math.max(1e-6, maxY - minY);
                    double svgW = 450, svgH = 450;

                    // Slightly tighter fit to make the curve larger
                    double sc = Math.min(svgW * 0.95 / worldW, svgH * 0.95 / worldH);
                    if (sc <= 0) sc = 1;

                    double tx = (svgW - worldW * sc) / 2.0;
                    double ty = (svgH - worldH * sc) / 2.0;

                    // Constant pixel stroke widths (do NOT divide by sc)
                    double gridStroke = 0.6;
                    double axisStroke = 1.5;
                    double spiralStroke = 2.8;

                    StringBuilder fullSvg = new StringBuilder();
                    fullSvg.append(String.format(
                            "<svg width=\"%f\" height=\"%f\" viewBox=\"0 0 %f %f\" xmlns=\"http://www.w3.org/2000/svg\">",
                            svgW, svgH, svgW, svgH));

                    // Apply transforms in standard order: translate -> scale -> translate(world)
                    // Note: right-to-left application: translate(-minX,-minY) happens first
                    fullSvg.append(String.format(
                            "<g transform=\"translate(%.3f, %.3f) scale(%.6f) translate(%.3f, %.3f)\">",
                            tx, ty, sc, -minX, -minY));

                    // Draw grid (lighter)
                    int gridSteps = 10;
                    double gridStepX = worldW / gridSteps;
                    double gridStepY = worldH / gridSteps;
                    for (int i = 0; i <= gridSteps; i++) {
                        double x = minX + i * gridStepX;
                        double y = minY + i * gridStepY;
                        fullSvg.append(String.format(
                                "<line x1=\"%.3f\" y1=\"%.3f\" x2=\"%.3f\" y2=\"%.3f\" stroke=\"#e8e8e8\" " +
                                        "stroke-width=\"%.2f\" vector-effect=\"non-scaling-stroke\" shape-rendering=\"crispEdges\"/>",
                                minX, y, maxX, y, gridStroke));
                        fullSvg.append(String.format(
                                "<line x1=\"%.3f\" y1=\"%.3f\" x2=\"%.3f\" y2=\"%.3f\" stroke=\"#e8e8e8\" " +
                                        "stroke-width=\"%.2f\" vector-effect=\"non-scaling-stroke\" shape-rendering=\"crispEdges\"/>",
                                x, minY, x, maxY, gridStroke));
                    }

                    // Draw axes
                    fullSvg.append(String.format(
                            "<line x1=\"%.3f\" y1=\"%.3f\" x2=\"%.3f\" y2=\"%.3f\" stroke=\"#000\" " +
                                    "stroke-width=\"%.2f\" vector-effect=\"non-scaling-stroke\"/>",
                            minX, 0.0, maxX, 0.0, axisStroke));
                    fullSvg.append(String.format(
                            "<line x1=\"%.3f\" y1=\"%.3f\" x2=\"%.3f\" y2=\"%.3f\" stroke=\"#000\" " +
                                    "stroke-width=\"%.2f\" vector-effect=\"non-scaling-stroke\"/>",
                            0.0, minY, 0.0, maxY, axisStroke));

                    // Draw Fibonacci spiral with constant visible thickness
                    fullSvg.append(String.format(
                            "<path d=\"%s\" stroke=\"#1f77b4\" stroke-width=\"%.2f\" fill=\"none\" " +
                                    "stroke-linecap=\"round\" vector-effect=\"non-scaling-stroke\"/>",
                            path.toString().trim(), spiralStroke));

                    fullSvg.append("</g>");
                    fullSvg.append("</svg>");

                    svg = fullSvg.toString();
                }
            }
        } catch (NumberFormatException e) {
            error = "Invalid n format (use numbers only).";
        } catch (Exception e) {
            error = "Unexpected error: " + e.getMessage();
            System.err.println("Fibonacci error: " + e.getMessage());
            e.printStackTrace();
        }

        model.addAttribute("error", error);
        model.addAttribute("svg", svg);
        model.addAttribute("n", nStr != null ? nStr.trim() : "");
        model.addAttribute("selectedOption", option);
        return "index";
    }
}
