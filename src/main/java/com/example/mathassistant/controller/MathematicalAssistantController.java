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
    public String index() {
        return "index";
    }

    @PostMapping("/calculate")
    public String calculate(@RequestParam("num1") String num1Str,
                            @RequestParam("num2") String num2Str,
                            @RequestParam("operation") String operation,
                            Model model) {
        String result = "";
        try {
            double num1 = Double.parseDouble(num1Str);
            double num2 = Double.parseDouble(num2Str);
            double res = 0;
            switch (operation) {
                case "+": res = num1 + num2; break;
                case "-": res = num1 - num2; break;
                case "*": res = num1 * num2; break;
                case "/":
                    if (num2 == 0) throw new ArithmeticException("Division by zero");
                    res = num1 / num2; break;
            }
            result = "Result: " + res;
        } catch (Exception e) {
            result = "Error: Invalid input or division by zero";
        }
        model.addAttribute("result", result);
        return "index";
    }

    @PostMapping("/fibonacci-curve")
    public String drawFibCurve(@RequestParam("n") String nStr, Model model) {
        String svg = "";
        try {
            int n = Integer.parseInt(nStr);
            if (n < 1) {
                model.addAttribute("error", "n must be at least 1");
                model.addAttribute("n", n); // For title
                return "index";
            }

            // Generate Fibonacci sequence
            List<Double> fibs = new ArrayList<>();
            if (n >= 1) fibs.add(1.0);
            if (n >= 2) fibs.add(1.0);
            for (int i = 2; i < n; i++) {
                double next = fibs.get(i - 1) + fibs.get(i - 2);
                fibs.add(next);
            }

            // Generate arcs and bounds
            double currX = 0, currY = 0;
            double theta_deg = 0;
            StringBuilder path = new StringBuilder("M 0 0 ");
            double minX = 0, maxX = 0, minY = 0, maxY = 0;

            for (double r : fibs) {
                double sin_th = Math.sin(Math.toRadians(theta_deg));
                double cos_th = Math.cos(Math.toRadians(theta_deg));
                double px = -sin_th;
                double py = cos_th;
                double cenX = currX + r * px;
                double cenY = currY + r * py;

                double vx = currX - cenX;
                double vy = currY - cenY;
                double start_ang = Math.toDegrees(Math.atan2(vy, vx));
                double end_ang = start_ang + 90;

                double sweep = 1;
                double large = 0;
                double rx = r;
                double ry = r;
                double xAxisRotation = 0;

                double endX = cenX + r * Math.cos(Math.toRadians(end_ang));
                double endY = cenY + r * Math.sin(Math.toRadians(end_ang));

                path.append(String.format("A %.3f %.3f %.0f %.0f %.0f %.3f %.3f ", rx, ry, xAxisRotation, large, sweep, endX, endY));

                currX = endX;
                currY = endY;
                theta_deg = (theta_deg + 90) % 360;

                minX = Math.min(minX, cenX - r);
                maxX = Math.max(maxX, cenX + r);
                minY = Math.min(minY, cenY - r);
                maxY = Math.max(maxY, cenY + r);
                minX = Math.min(minX, currX);
                maxX = Math.max(maxX, currX);
                minY = Math.min(minY, currY);
                maxY = Math.max(maxY, currY);
            }

            // Calculate scale
            double worldW = maxX - minX;
            double worldH = maxY - minY;
            if (worldW == 0) worldW = 1;
            if (worldH == 0) worldH = 1;
            double svgW = 450; // Inner SVG size (room for axes)
            double svgH = 450;
            double scX = svgW * 0.9 / worldW;
            double scY = svgH * 0.9 / worldH;
            double sc = Math.min(scX, scY);
            double tx = (svgW - worldW * sc) / 2;
            double ty = (svgH - worldH * sc) / 2;

            // Build SVG with axes, grid, title
            StringBuilder fullSvg = new StringBuilder();
            fullSvg.append(String.format("<svg width=\"%f\" height=\"%f\" viewBox=\"0 0 %f %f\" xmlns=\"http://www.w3.org/2000/svg\">", svgW, svgH, svgW, svgH));
            fullSvg.append(String.format("<g transform=\"translate(%.3f, %.3f) scale(%.3f) translate(%.3f, %.3f)\">", tx, ty, sc, -minX, -minY));

            // Grid (light gray lines)
            int gridSteps = 10;
            double gridStepX = worldW / gridSteps;
            double gridStepY = worldH / gridSteps;
            for (int i = 0; i <= gridSteps; i++) {
                double x = minX + i * gridStepX;
                double y = minY + i * gridStepY;
                fullSvg.append(String.format("<line x1=\"%.3f\" y1=\"%.3f\" x2=\"%.3f\" y2=\"%.3f\" stroke=\"#e0e0e0\" stroke-width=\"0.5\"/>", minX, y, maxX, y));
                fullSvg.append(String.format("<line x1=\"%.3f\" y1=\"%.3f\" x2=\"%.3f\" y2=\"%.3f\" stroke=\"#e0e0e0\" stroke-width=\"0.5\"/>", x, minY, x, maxY));
            }

            // Axes (black lines)
            fullSvg.append(String.format("<line x1=\"%.3f\" y1=\"%.3f\" x2=\"%.3f\" y2=\"%.3f\" stroke=\"black\" stroke-width=\"1.5\"/>", minX, 0, maxX, 0)); // X-axis
            fullSvg.append(String.format("<line x1=\"%.3f\" y1=\"%.3f\" x2=\"%.3f\" y2=\"%.3f\" stroke=\"black\" stroke-width=\"1.5\"/>", 0, minY, 0, maxY)); // Y-axis

            // Curve path (blue)
            fullSvg.append(String.format("<path d=\"%s\" stroke=\"#1f77b4\" stroke-width=\"2.5\" fill=\"none\" stroke-linecap=\"round\"/>", path.toString()));

            fullSvg.append("</g></svg>");

            svg = fullSvg.toString();
            model.addAttribute("svg", svg);
            model.addAttribute("n", n); // For title

        } catch (Exception e) {
            model.addAttribute("error", "Error: Invalid n or too large.");
            model.addAttribute("n", nStr); // For title
            return "index";
        }
        return "index";
    }
}