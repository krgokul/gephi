/*
Copyright 2008-2011 Gephi
Authors : Yudi Xue <yudi.xue@usask.ca>, Mathieu Bastian
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.preview.plugin.renderers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import org.gephi.graph.api.Edge;
import org.gephi.preview.api.Item;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.ProcessingTarget;
import org.gephi.preview.api.RenderTarget;
import org.gephi.preview.api.SVGTarget;

import org.gephi.preview.plugin.items.EdgeItem;
import org.gephi.preview.plugin.items.EdgeLabelItem;

import org.gephi.preview.plugin.items.NodeItem;
import org.gephi.preview.spi.Renderer;
import org.gephi.preview.types.DependantOriginalColor;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import processing.core.PGraphics;
import processing.core.PGraphicsJava2D;
import processing.core.PVector;

/**
 *
 * @author Yudi Xue, Mathieu Bastian
 */
@ServiceProvider(service = Renderer.class, position = 500)
public class EdgeLabelRenderer implements Renderer {
    //Custom properties

    public static final String EDGE_COLOR = "edge.label.edgeColor";
    public static final String LABEL_X = "edge.label.x";
    public static final String LABEL_Y = "edge.label.y";
    //Default values
    private final boolean defaultShowLabels = true;
    private final Font defaultFont = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
    private final boolean defaultShorten = false;
    private final DependantOriginalColor defaultColor = new DependantOriginalColor(DependantOriginalColor.Mode.ORIGINAL);
    private final int defaultMaxChar = 30;
    private final float defaultOutlineSize = 2;
    private final Color defaultOutlineColor = Color.WHITE;
    private final float defaultOutlineTransparency = 0.6f;
    //Font cache
    private Font font;

    public void preProcess(PreviewModel previewModel) {
        PreviewProperties properties = previewModel.getProperties();
        if (properties.getBooleanValue(PreviewProperty.EDGE_LABEL_SHORTEN)) {
            //Shorten labels
            Item[] EdgeLabelsItems = previewModel.getItems(Item.EDGE_LABEL);

            int maxChars = properties.getIntValue(PreviewProperty.EDGE_LABEL_MAX_CHAR);
            for (Item item : EdgeLabelsItems) {
                String label = item.getData(EdgeLabelItem.LABEL);
                if (label.length() >= maxChars + 3) {
                    label = label.substring(0, maxChars) + "...";
                    item.setData(EdgeLabelItem.LABEL, label);
                }
            }
        }

        //Put parent color, and calculate position
        for (Item item : previewModel.getItems(Item.EDGE_LABEL)) {
            Edge edge = (Edge) item.getSource();
            Item edgeItem = previewModel.getItem(Item.EDGE, edge);
            item.setData(EDGE_COLOR, edgeItem.getData(EdgeItem.COLOR));
            NodeItem sourceItem = (NodeItem) edgeItem.getData(EdgeRenderer.SOURCE);
            NodeItem targetItem = (NodeItem) edgeItem.getData(EdgeRenderer.TARGET);
            if (edge.isSelfLoop()) {
                //Middle
                Float x = sourceItem.getData(NodeItem.X);
                Float y = sourceItem.getData(NodeItem.Y);
                Float size = sourceItem.getData(NodeItem.SIZE);

                PVector v1 = new PVector(x, y);
                v1.add(size, -size, 0);

                PVector v2 = new PVector(x, y);
                v2.add(size, size, 0);

                PVector middle = bezierPoint(x, y, v1.x, v1.y, v2.x, v2.y, x, y, 0.5f);
                item.setData(LABEL_X, middle.x);
                item.setData(LABEL_Y, middle.y);

            } else if (properties.getBooleanValue(PreviewProperty.EDGE_CURVED)) {
                //Middle of the curve
                Float x1 = sourceItem.getData(NodeItem.X);
                Float x2 = targetItem.getData(NodeItem.X);
                Float y1 = sourceItem.getData(NodeItem.Y);
                Float y2 = targetItem.getData(NodeItem.Y);

                //Curved edgs
                PVector direction = new PVector(x2, y2);
                direction.sub(new PVector(x1, y1));
                float length = direction.mag();
                direction.normalize();

                float factor = properties.getFloatValue(EdgeRenderer.BEZIER_CURVENESS) * length;

                // normal vector to the edge
                PVector n = new PVector(direction.y, -direction.x);
                n.mult(factor);

                // first control point
                PVector v1 = new PVector(direction.x, direction.y);
                v1.mult(factor);
                v1.add(new PVector(x1, y1));
                v1.add(n);

                // second control point
                PVector v2 = new PVector(direction.x, direction.y);
                v2.mult(-factor);
                v2.add(new PVector(x2, y2));
                v2.add(n);

                PVector middle = bezierPoint(x1, y1, v1.x, v1.y, v2.x, v2.y, x2, y2, 0.5f);
                item.setData(LABEL_X, middle.x);
                item.setData(LABEL_Y, middle.y);
            } else {
                Float x = ((Float) sourceItem.getData(NodeItem.X) + (Float) targetItem.getData(NodeItem.X)) / 2f;
                Float y = ((Float) sourceItem.getData(NodeItem.Y) + (Float) targetItem.getData(NodeItem.Y)) / 2f;
                item.setData(LABEL_X, x);
                item.setData(LABEL_Y, y);
            }
        }

        //Property font
        font = properties.getFontValue(PreviewProperty.EDGE_LABEL_FONT);
    }

    public void render(Item item, RenderTarget target, PreviewProperties properties) {
        Edge edge = (Edge) item.getSource();
        //Label
        Color EdgeColor = item.getData(EDGE_COLOR);
        Color color = item.getData(EdgeLabelItem.COLOR);
        DependantOriginalColor propColor = properties.getValue(PreviewProperty.EDGE_LABEL_COLOR);
        color = propColor.getColor(EdgeColor, color);
        String label = item.getData(EdgeLabelItem.LABEL);
        Float x = item.getData(LABEL_X);
        Float y = item.getData(LABEL_Y);

        //Outline
        Color outlineColor = properties.getColorValue(PreviewProperty.EDGE_LABEL_OUTLINE_COLOR);
        Float outlineSize = properties.getFloatValue(PreviewProperty.EDGE_LABEL_OUTLINE_SIZE);
        outlineSize = outlineSize * (font.getSize() / 32f);
        Float outlineTransparency = properties.getFloatValue(PreviewProperty.EDGE_LABEL_OUTLINE_TRANSPARENCY);

        if (target instanceof ProcessingTarget) {
            renderProcessing((ProcessingTarget) target, label, x, y, color, outlineSize, outlineColor, outlineTransparency);
        }
    }

    public void renderProcessing(ProcessingTarget target, String label, float x, float y, Color color, float outlineSize, Color outlineColor, float outlineTransparency) {
        PGraphics graphics = target.getGraphics();
        Graphics2D g2 = ((PGraphicsJava2D) graphics).g2;
        graphics.textAlign(PGraphics.CENTER, PGraphics.CENTER);

        g2.setFont(font);

        FontMetrics fm = g2.getFontMetrics();
        float posX = x - fm.stringWidth(label) / 2f;
        float posY = y + fm.getAscent() / 2f;

        if (outlineSize > 0) {
            FontRenderContext frc = g2.getFontRenderContext();
            GlyphVector gv = font.createGlyphVector(frc, label);
            Shape glyph = gv.getOutline(posX, posY);
            g2.setColor(outlineColor);
            g2.setStroke(new BasicStroke(outlineSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(glyph);
        }

        g2.setColor(color);
        g2.drawString(label, posX, posY);
    }

    public void renderSVG(SVGTarget target, Edge Edge, String label, float x, float y, Color color, float outlineSize, Color outlineColor, float outlineTransparency) {
        Text labelText = target.createTextNode(label);

        Element labelElem = target.createElement("text");
        labelElem.setAttribute("class", Edge.getEdgeData().getId());
        labelElem.setAttribute("x", x + "");
        labelElem.setAttribute("y", y + "");
        labelElem.setAttribute("style", "text-anchor: middle");
        labelElem.setAttribute("fill", target.toHexString(color));
        labelElem.setAttribute("font-family", font.getFamily());
        labelElem.setAttribute("font-size", font.getSize() + "");
        labelElem.appendChild(labelText);
        target.getTopElement(SVGTarget.TOP_EDGE_LABELS);
    }

    public PreviewProperty[] getProperties() {
        return new PreviewProperty[]{
                    PreviewProperty.createProperty(this, PreviewProperty.SHOW_EDGE_LABELS, Boolean.class,
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.property.display.displayName"),
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.property.display.description"),
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.category")).setValue(defaultShowLabels),
                    PreviewProperty.createProperty(this, PreviewProperty.EDGE_LABEL_FONT, Font.class,
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.property.font.displayName"),
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.property.font.description"),
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.category"), PreviewProperty.SHOW_EDGE_LABELS).setValue(defaultFont),
                    PreviewProperty.createProperty(this, PreviewProperty.EDGE_LABEL_COLOR, DependantOriginalColor.class,
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.property.color.displayName"),
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.property.color.description"),
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.category"), PreviewProperty.SHOW_EDGE_LABELS).setValue(defaultColor),
                    PreviewProperty.createProperty(this, PreviewProperty.EDGE_LABEL_SHORTEN, Boolean.class,
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.property.shorten.displayName"),
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.property.shorten.description"),
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.category"), PreviewProperty.SHOW_EDGE_LABELS).setValue(defaultShorten),
                    PreviewProperty.createProperty(this, PreviewProperty.EDGE_LABEL_MAX_CHAR, Integer.class,
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.property.maxchar.displayName"),
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.property.maxchar.description"),
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.category"), PreviewProperty.SHOW_EDGE_LABELS).setValue(defaultMaxChar),
                    PreviewProperty.createProperty(this, PreviewProperty.EDGE_LABEL_OUTLINE_SIZE, Float.class,
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.property.outlineSize.displayName"),
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.property.outlineSize.description"),
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.category"), PreviewProperty.SHOW_EDGE_LABELS).setValue(defaultOutlineSize),
                    PreviewProperty.createProperty(this, PreviewProperty.EDGE_LABEL_OUTLINE_COLOR, Color.class,
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.property.outlineColor.displayName"),
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.property.outlineColor.description"),
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.category"), PreviewProperty.SHOW_EDGE_LABELS).setValue(defaultOutlineColor),
                    PreviewProperty.createProperty(this, PreviewProperty.EDGE_LABEL_OUTLINE_TRANSPARENCY, Float.class,
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.property.outlineTransparency.displayName"),
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.property.outlineTransparency.description"),
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.category"), PreviewProperty.SHOW_EDGE_LABELS).setValue(defaultOutlineTransparency),};
    }

    public boolean isRendererForitem(Item item, PreviewProperties properties) {
        return item instanceof EdgeLabelItem && properties.getBooleanValue(PreviewProperty.SHOW_EDGE_LABELS)
                && !properties.getBooleanValue(PreviewProperty.MOVING);
    }

    private PVector bezierPoint(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4, float c) {
        PVector ab = linearInterpolation(x1, y1, x2, y2, c);
        PVector bc = linearInterpolation(x2, y2, x3, y3, c);
        PVector cd = linearInterpolation(x3, y3, x4, y4, c);
        PVector abbc = linearInterpolation(ab.x, ab.y, bc.x, bc.y, c);
        PVector bccd = linearInterpolation(bc.x, bc.y, cd.x, cd.y, c);
        return linearInterpolation(abbc.x, abbc.y, bccd.x, bccd.y, c);
    }

    public PVector linearInterpolation(float x1, float y1, float x2, float y2, float c) {
        PVector r = new PVector(x1 + (x2 - x1) * c, y1 + (y2 - y1) * c);
        return r;
    }
}