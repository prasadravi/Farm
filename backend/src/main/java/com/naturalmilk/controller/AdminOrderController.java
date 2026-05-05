package com.naturalmilk.controller;

import java.io.ByteArrayOutputStream;
import java.awt.Color;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.naturalmilk.model.Order;
import com.naturalmilk.service.OrderService;

@Controller
public class AdminOrderController {
    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/admin/orders")
    public String orders(Model model) {
        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        return "admin/orders";
    }

    @GetMapping("/admin/orders/download-pdf")
    public ResponseEntity<byte[]> downloadOrdersPdf() throws DocumentException {
        List<Order> orders = orderService.getAllOrders();
        byte[] pdfBytes = buildOrdersPdf(orders);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("orders-report.pdf").build());
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @PostMapping("/admin/orders/{id}/status")
    public String updateStatus(@PathVariable String id, @RequestParam String status) {
        orderService.updateOrderStatus(id, status);
        return "redirect:/admin/orders";
    }

    @PostMapping("/admin/orders/{id}/delete")
    public String deleteOrder(@PathVariable String id) {
        orderService.deleteOrder(id);
        return "redirect:/admin/orders";
    }

    private byte[] buildOrdersPdf(List<Order> orders) throws DocumentException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);

            Paragraph title = new Paragraph("Natural Milk Orders Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(6f);
            document.add(title);

            Paragraph subtitle = new Paragraph(
                "Generated on " + DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")
                    .withZone(ZoneId.systemDefault())
                    .format(Instant.now()),
                subtitleFont
            );
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(16f);
            document.add(subtitle);

            PdfPTable table = new PdfPTable(new float[] { 1.8f, 1.4f, 2.4f, 1.0f, 1.1f, 1.0f, 1.2f });
            table.setWidthPercentage(100f);
            table.setSpacingBefore(4f);

            addHeaderCell(table, "Order ID", headerFont);
            addHeaderCell(table, "Customer", headerFont);
            addHeaderCell(table, "Address", headerFont);
            addHeaderCell(table, "Amount", headerFont);
            addHeaderCell(table, "Payment", headerFont);
            addHeaderCell(table, "Status", headerFont);
            addHeaderCell(table, "Created", headerFont);

            for (Order order : orders) {
                addCell(table, safeValue(order.getId()), cellFont);
                addCell(table, safeValue(order.getUserId()), cellFont);
                addCell(table, formatAddress(order), cellFont);
                addCell(table, "₹" + String.format("%.2f", order.getTotal()), cellFont);
                addCell(table, order.getPayment() != null ? safeValue(order.getPayment().getStatus()) : "NA", cellFont);
                addCell(table, safeValue(order.getStatus()), cellFont);
                addCell(table, formatTime(order.getCreatedAt()), cellFont);
            }

            if (orders.isEmpty()) {
                PdfPCell emptyCell = new PdfPCell(new Phrase("No orders found", cellFont));
                emptyCell.setColspan(7);
                emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                emptyCell.setPadding(12f);
                table.addCell(emptyCell);
            }

            document.add(table);
            document.close();
            return outputStream.toByteArray();
        } catch (Exception ex) {
            if (ex instanceof DocumentException documentException) {
                throw documentException;
            }
            throw new IllegalStateException("Failed to generate orders PDF", ex);
        }
    }

    private void addHeaderCell(PdfPTable table, String label, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(label, font));
        cell.setBackgroundColor(new Color(34, 34, 34));
        cell.setPadding(8f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String value, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(value, font));
        cell.setPadding(8f);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private String safeValue(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }

    private String formatAddress(Order order) {
        StringBuilder builder = new StringBuilder();
        appendPart(builder, order.getAddress());
        appendPart(builder, order.getLandmark());
        appendPart(builder, order.getPincode());
        appendPart(builder, order.getPhone());
        return builder.length() == 0 ? "-" : builder.toString();
    }

    private void appendPart(StringBuilder builder, String value) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(" | ");
        }
        builder.append(value.trim());
    }

    private String formatTime(long timestamp) {
        return DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")
            .withZone(ZoneId.systemDefault())
            .format(Instant.ofEpochMilli(timestamp));
    }
}
