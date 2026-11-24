package com.icesi.ui;

import com.icesi.controller.GameController;
import com.icesi.model.Incident;
import com.icesi.model.Severity;
import com.icesi.structures.BSTNode;
import com.icesi.structures.BinarySearchTree;
import com.icesi.structures.LinkedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.icesi.ui.MonitoringCenterScreen.pixelButton;
import static com.icesi.ui.MonitoringCenterScreen.styled;

// Pantalla del Arbol Binario (BST): visualiza la estructura jerarquica de prioridad de incidentes activos.
public class BinaryTreeScreen implements GameScreen {

    public enum TraversalMode {
        IN_ORDER, PRE_ORDER, POST_ORDER
    }

    private GameController controller;
    private Consumer<String> navigator;
    private Runnable onOpenRegistration;

    private BorderPane root;
    private Canvas treeCanvas;
    private ScrollPane scrollPane;
    private StackPane canvasContainer;
    private EmptyStateView emptyStateView;

    private CountingLabel nodeCountValue;
    private CountingLabel treeHeightValue;
    private Label balanceFactorLabel;
    private Label traversalExplanationLabel;
    private Label traversalSequenceLabel;

    private Button inOrderBtn;
    private Button preOrderBtn;
    private Button postOrderBtn;
    private TraversalMode currentMode = TraversalMode.IN_ORDER;

    // Inspector de incidente seleccionado
    private Label selectedIdLabel;
    private Label selectedTypeLabel;
    private Label selectedSeverityLabel;
    private Label selectedLocationLabel;
    private Label selectedStatusLabel;
    private Label selectedVehicleLabel;
    private Incident selectedIncident;

    // Mapa de posiciones de nodos dibujados para deteccion de clics
    private static class NodeBox {
        double x, y, w, h;
        Incident incident;

        NodeBox(double x, double y, double w, double h, Incident incident) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.incident = incident;
        }

        boolean contains(double px, double py) {
            return px >= x && px <= x + w && py >= y && py <= y + h;
        }
    }

    private List<NodeBox> renderedNodes = new ArrayList<>();

    public BinaryTreeScreen(GameController controller, Consumer<String> navigator) {
        this.controller = controller;
        this.navigator = navigator;

        root = new BorderPane();
        root.setPadding(new Insets(10, 16, 12, 16));
        root.getStyleClass().add("crt-background");

        // 1. Cabecera superior
        VBox topBox = createHeader();
        root.setTop(topBox);

        // 2. Area central del arbol
        treeCanvas = new Canvas(1100, 500);
        canvasContainer = new StackPane(treeCanvas);
        canvasContainer.setAlignment(Pos.CENTER);
        canvasContainer.setStyle("-fx-background-color: #0b0f1d; -fx-border-color: #263353; -fx-border-width: 2;");

        scrollPane = new ScrollPane(canvasContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: #0b0f1d; -fx-background-color: transparent; -fx-border-color: transparent;");

        emptyStateView = new EmptyStateView();

        StackPane centerSlot = new StackPane(scrollPane, emptyStateView);
        VBox.setVgrow(centerSlot, Priority.ALWAYS);
        root.setCenter(centerSlot);

        // 3. Panel inferior de inspeccion y recorrido
        VBox bottomBox = createBottomInspector();
        root.setBottom(bottomBox);

        // Interaccion con el raton para seleccionar nodos
        treeCanvas.setOnMouseClicked(event -> {
            double mx = event.getX();
            double my = event.getY();
            for (NodeBox box : renderedNodes) {
                if (box.contains(mx, my)) {
                    selectedIncident = box.incident;
                    RetroSound.play(RetroSound.Effect.MENU);
                    updateInspector();
                    drawTree();
                    break;
                }
            }
        });
    }

    private VBox createHeader() {
        Canvas title = PixelFont.createLabel("ARBOL BINARIO BST", 5, RetroTheme.YELLOW, RetroTheme.PURPLE);
        Label subtitle = styled(new Label("JERARQUIA DE PRIORIDAD DE INCIDENTES ACTIVOS (ORDEN DE ATENCION)"), "panel-subtitle");

        Button centerBtn = pixelButton("[C] CENTRO", null);
        centerBtn.setOnAction(e -> navigator.accept("center"));

        Button mapBtn = pixelButton("[M] MAPA", "button-blue");
        mapBtn.setOnAction(e -> navigator.accept("map"));

        Button incidentsBtn = pixelButton("[I] INCIDENTES", "button-yellow");
        incidentsBtn.setOnAction(e -> navigator.accept("incidents"));

        Button newIncidentBtn = pixelButton("[+] NUEVO INCIDENTE", "button-green");
        newIncidentBtn.setOnAction(e -> {
            if (onOpenRegistration != null) {
                onOpenRegistration.run();
            }
        });

        Region push = new Region();
        HBox.setHgrow(push, Priority.ALWAYS);
        HBox navBar = new HBox(10, centerBtn, mapBtn, incidentsBtn, push, newIncidentBtn);
        navBar.setAlignment(Pos.CENTER_LEFT);

        // Barra de estadisticas del BST y selector de recorridos
        nodeCountValue = new CountingLabel("%02d");
        treeHeightValue = new CountingLabel("%02d");
        balanceFactorLabel = styled(new Label("EQUILIBRADO"), "stat-value");

        HBox statsBox = new HBox(16,
                createStatChip("TOTAL NODOS", nodeCountValue),
                createStatChip("ALTURA BST", treeHeightValue),
                createStatChip("ESTADO", balanceFactorLabel)
        );
        statsBox.setAlignment(Pos.CENTER_LEFT);

        inOrderBtn = pixelButton("[1] IN-ORDER", "button-yellow");
        inOrderBtn.setOnAction(e -> setTraversalMode(TraversalMode.IN_ORDER));

        preOrderBtn = pixelButton("[2] PRE-ORDER", null);
        preOrderBtn.setOnAction(e -> setTraversalMode(TraversalMode.PRE_ORDER));

        postOrderBtn = pixelButton("[3] POST-ORDER", null);
        postOrderBtn.setOnAction(e -> setTraversalMode(TraversalMode.POST_ORDER));

        HBox traversalBtns = new HBox(8, styled(new Label("RECORRIDO:"), "panel-title"), inOrderBtn, preOrderBtn, postOrderBtn);
        traversalBtns.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox controlRibbon = new HBox(16, statsBox, spacer, traversalBtns);
        controlRibbon.setAlignment(Pos.CENTER_LEFT);
        controlRibbon.setPadding(new Insets(6, 0, 6, 0));

        VBox header = new VBox(6, title, subtitle, navBar, controlRibbon);
        header.setPadding(new Insets(0, 0, 6, 0));
        return header;
    }

    private HBox createStatChip(String title, Node value) {
        VBox chip = new VBox(2);
        chip.getStyleClass().add("stat-card");
        chip.setPadding(new Insets(4, 10, 4, 10));
        chip.getChildren().addAll(styled(new Label(title), "panel-subtitle"), value);
        return new HBox(chip);
    }

    private VBox createBottomInspector() {
        VBox bottom = new VBox(8);
        bottom.setPadding(new Insets(8, 0, 0, 0));

        selectedIdLabel = styled(new Label("SELECCIONA UN NODO"), "stat-value");
        selectedTypeLabel = styled(new Label("-"), "stat-name");
        selectedSeverityLabel = styled(new Label("-"), "stat-name");
        selectedLocationLabel = styled(new Label("-"), "stat-name");
        selectedStatusLabel = styled(new Label("-"), "stat-name");
        selectedVehicleLabel = styled(new Label("-"), "stat-name");

        HBox inspectorBar = new HBox(18,
                new VBox(2, styled(new Label("ID INCIDENTE"), "panel-subtitle"), selectedIdLabel),
                new VBox(2, styled(new Label("TIPO"), "panel-subtitle"), selectedTypeLabel),
                new VBox(2, styled(new Label("GRAVEDAD"), "panel-subtitle"), selectedSeverityLabel),
                new VBox(2, styled(new Label("UBICACION"), "panel-subtitle"), selectedLocationLabel),
                new VBox(2, styled(new Label("ESTADO"), "panel-subtitle"), selectedStatusLabel),
                new VBox(2, styled(new Label("UNIDAD ASIGNADA"), "panel-subtitle"), selectedVehicleLabel)
        );
        inspectorBar.setAlignment(Pos.CENTER_LEFT);
        inspectorBar.getStyleClass().add("pixel-panel");
        inspectorBar.setPadding(new Insets(6, 12, 6, 12));

        // Cinta de secuencia de recorrido
        traversalSequenceLabel = styled(new Label(""), "dialog-text");
        traversalExplanationLabel = styled(new Label(""), "panel-subtitle");

        VBox sequenceBox = new VBox(4, traversalExplanationLabel, traversalSequenceLabel);
        sequenceBox.getStyleClass().add("dialog-box");
        sequenceBox.setPadding(new Insets(6, 10, 6, 10));

        bottom.getChildren().addAll(inspectorBar, sequenceBox);
        return bottom;
    }

    private void setTraversalMode(TraversalMode mode) {
        this.currentMode = mode;
        inOrderBtn.getStyleClass().removeAll("button-yellow", "button-blue");
        preOrderBtn.getStyleClass().removeAll("button-yellow", "button-blue");
        postOrderBtn.getStyleClass().removeAll("button-yellow", "button-blue");

        if (mode == TraversalMode.IN_ORDER) {
            inOrderBtn.getStyleClass().add("button-yellow");
        } else if (mode == TraversalMode.PRE_ORDER) {
            preOrderBtn.getStyleClass().add("button-yellow");
        } else {
            postOrderBtn.getStyleClass().add("button-yellow");
        }

        RetroSound.play(RetroSound.Effect.MENU);
        updateTraversalSequence();
    }

    private void updateTraversalSequence() {
        BinarySearchTree<Incident> tree = controller.getActiveIncidentsTree();
        if (tree == null || tree.isEmpty()) {
            traversalSequenceLabel.setText("(Arbol vacio)");
            traversalExplanationLabel.setText("No hay elementos para recorrer.");
            return;
        }

        LinkedList<Incident> list;
        if (currentMode == TraversalMode.IN_ORDER) {
            list = tree.inOrder();
            traversalExplanationLabel.setText("RECORRIDO IN-ORDER (IZQ → RAIZ → DER): Orden natural de atencion (mayor a menor urgencia).");
        } else if (currentMode == TraversalMode.PRE_ORDER) {
            list = tree.preOrder();
            traversalExplanationLabel.setText("RECORRIDO PRE-ORDER (RAIZ → IZQ → DER): Estructura jerárquica desde la cúspide.");
        } else {
            list = tree.postOrder();
            traversalExplanationLabel.setText("RECORRIDO POST-ORDER (IZQ → DER → RAIZ): De abajo hacia arriba (hojas a raíz).");
        }

        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (Incident inc : list) {
            if (count > 0) {
                sb.append("  ➜  ");
            }
            sb.append("[").append(inc.getId()).append(" - ").append(inc.getSeverity()).append("]");
            count++;
        }
        traversalSequenceLabel.setText(sb.toString());
    }

    @Override
    public Node getView() {
        refresh();
        return root;
    }

    @Override
    public void refresh() {
        BinarySearchTree<Incident> tree = controller.getActiveIncidentsTree();
        boolean empty = (tree == null || tree.isEmpty());
        emptyStateView.setVisible(empty);
        scrollPane.setVisible(!empty);

        if (empty) {
            nodeCountValue.setValue(0);
            treeHeightValue.setValue(0);
            balanceFactorLabel.setText("VACIO");
            traversalSequenceLabel.setText("No hay incidentes activos en el arbol BST.");
            traversalExplanationLabel.setText("Genera o registra un nuevo incidente para poblar el arbol.");
            selectedIncident = null;
            updateInspector();
            return;
        }

        nodeCountValue.setValue(tree.size());
        int height = tree.height();
        treeHeightValue.setValue(height);

        // Estimacion de balance
        int minHeight = (int) Math.ceil(Math.log(tree.size() + 1) / Math.log(2));
        if (height <= minHeight + 1) {
            balanceFactorLabel.setText("OPTIMO");
        } else if (height <= minHeight + 2) {
            balanceFactorLabel.setText("ACEPTABLE");
        } else {
            balanceFactorLabel.setText("DESBALANCEADO");
        }

        // Si no hay incidente seleccionado o ya no esta activo, selecciona la raiz
        if (selectedIncident == null && tree.getRoot() != null) {
            selectedIncident = tree.getRoot().getValue();
        }

        updateInspector();
        updateTraversalSequence();
        drawTree();
    }

    private void updateInspector() {
        if (selectedIncident == null) {
            selectedIdLabel.setText("NINGUNO");
            selectedTypeLabel.setText("-");
            selectedSeverityLabel.setText("-");
            selectedLocationLabel.setText("-");
            selectedStatusLabel.setText("-");
            selectedVehicleLabel.setText("-");
            return;
        }

        selectedIdLabel.setText(selectedIncident.getId());
        selectedTypeLabel.setText(Texts.of(selectedIncident.getType()));
        selectedSeverityLabel.setText(selectedIncident.getSeverity().name());
        selectedLocationLabel.setText(selectedIncident.getLocation());
        selectedStatusLabel.setText(selectedIncident.getStatus().name());
        selectedVehicleLabel.setText(selectedIncident.getAssignedVehicle() != null ?
                selectedIncident.getAssignedVehicle().getId() : "SIN ASIGNAR");
    }

    // Dibuja el arbol BST usando el algoritmo in-order para distribuir coordenadas X sin colisiones
    private void drawTree() {
        BinarySearchTree<Incident> tree = controller.getActiveIncidentsTree();
        if (tree == null || tree.isEmpty()) {
            return;
        }

        BSTNode<Incident> rootNode = tree.getRoot();
        int size = tree.size();
        int height = tree.height();

        double nodeW = 100;
        double nodeH = 46;
        double levelGap = 80;

        double canvasW = Math.max(1000, (size + 1) * 115);
        double canvasH = Math.max(480, (height + 1) * levelGap + 50);

        treeCanvas.setWidth(canvasW);
        treeCanvas.setHeight(canvasH);

        GraphicsContext gc = treeCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvasW, canvasH);

        // Fondo y rejilla pixel retro
        gc.setFill(Color.web("#0c1020"));
        gc.fillRect(0, 0, canvasW, canvasH);
        gc.setFill(Color.web("#141b34"));
        for (double gx = 0; gx < canvasW; gx += 20) {
            for (double gy = 0; gy < canvasH; gy += 20) {
                gc.fillRect(gx, gy, 1, 1);
            }
        }

        // 1. Asignar indices X segun recorrido In-Order (garantiza orden estricto de izquierda a derecha)
        Map<BSTNode<Incident>, Integer> inOrderIndices = new HashMap<>();
        int[] counter = new int[]{0};
        collectInOrder(rootNode, inOrderIndices, counter);

        // 2. Calcular coordenadas absolutas
        Map<BSTNode<Incident>, double[]> positions = new HashMap<>();
        double xStep = canvasW / (size + 1);
        calculatePositions(rootNode, 0, inOrderIndices, xStep, levelGap, nodeW, positions);

        // 3. Dibujar lineas de conexion entre padres e hijos
        renderedNodes.clear();
        drawBranches(gc, rootNode, positions, nodeW, nodeH);

        // 4. Dibujar tarjetas de nodos
        drawNodes(gc, rootNode, positions, nodeW, nodeH);
    }

    private void collectInOrder(BSTNode<Incident> node, Map<BSTNode<Incident>, Integer> indices, int[] counter) {
        if (node == null) return;
        collectInOrder(node.getLeft(), indices, counter);
        indices.put(node, counter[0]++);
        collectInOrder(node.getRight(), indices, counter);
    }

    private void calculatePositions(BSTNode<Incident> node, int depth, Map<BSTNode<Incident>, Integer> indices,
                                    double xStep, double levelGap, double nodeW, Map<BSTNode<Incident>, double[]> positions) {
        if (node == null) return;
        int idx = indices.get(node);
        double x = (idx + 1) * xStep - nodeW / 2;
        double y = 40 + depth * levelGap;
        positions.put(node, new double[]{x, y});

        calculatePositions(node.getLeft(), depth + 1, indices, xStep, levelGap, nodeW, positions);
        calculatePositions(node.getRight(), depth + 1, indices, xStep, levelGap, nodeW, positions);
    }

    private void drawBranches(GraphicsContext gc, BSTNode<Incident> node, Map<BSTNode<Incident>, double[]> positions,
                              double nodeW, double nodeH) {
        if (node == null) return;
        double[] pos = positions.get(node);
        double px = pos[0] + nodeW / 2;
        double py = pos[1] + nodeH;

        if (node.getLeft() != null) {
            double[] leftPos = positions.get(node.getLeft());
            double cx = leftPos[0] + nodeW / 2;
            double cy = leftPos[1];

            // Rama izquierda: Cian
            gc.setStroke(Color.web("#29adff"));
            gc.setLineWidth(2);
            gc.strokeLine(px, py, cx, cy);

            // Etiqueta L (menor)
            drawBranchLabel(gc, (px + cx) / 2 - 10, (py + cy) / 2 - 8, "[L] <", Color.web("#29adff"));
            drawBranches(gc, node.getLeft(), positions, nodeW, nodeH);
        }

        if (node.getRight() != null) {
            double[] rightPos = positions.get(node.getRight());
            double cx = rightPos[0] + nodeW / 2;
            double cy = rightPos[1];

            // Rama derecha: Amarillo
            gc.setStroke(Color.web("#ffec27"));
            gc.setLineWidth(2);
            gc.strokeLine(px, py, cx, cy);

            // Etiqueta R (mayor)
            drawBranchLabel(gc, (px + cx) / 2 - 10, (py + cy) / 2 - 8, "[R] >", Color.web("#ffec27"));
            drawBranches(gc, node.getRight(), positions, nodeW, nodeH);
        }
    }

    private void drawBranchLabel(GraphicsContext gc, double x, double y, String text, Color color) {
        gc.setFill(Color.rgb(12, 16, 32, 0.85));
        gc.fillRect(x, y, 28, 14);
        gc.setStroke(color);
        gc.setLineWidth(1);
        gc.strokeRect(x, y, 28, 14);
        gc.setFill(color);
        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 9));
        gc.fillText(text, x + 3, y + 10);
    }

    private void drawNodes(GraphicsContext gc, BSTNode<Incident> node, Map<BSTNode<Incident>, double[]> positions,
                           double nodeW, double nodeH) {
        if (node == null) return;
        double[] pos = positions.get(node);
        double x = pos[0];
        double y = pos[1];
        Incident incident = node.getValue();

        renderedNodes.add(new NodeBox(x, y, nodeW, nodeH, incident));

        boolean isSelected = (selectedIncident != null && selectedIncident.getId().equals(incident.getId()));

        // Sombra pixel
        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.fillRect(x + 3, y + 3, nodeW, nodeH);

        // Fondo de tarjeta
        gc.setFill(Color.web("#151c36"));
        gc.fillRect(x, y, nodeW, nodeH);

        // Franja de severidad superior
        Color sevColor = colorOf(incident.getSeverity());
        gc.setFill(sevColor);
        gc.fillRect(x, y, nodeW, 6);

        // Borde exterior
        gc.setStroke(isSelected ? RetroTheme.YELLOW : Color.web("#3f4f7a"));
        gc.setLineWidth(isSelected ? 2.5 : 1.5);
        gc.strokeRect(x, y, nodeW, nodeH);

        // Icono sprite
        SpriteRegistry.draw(gc, SpriteRegistry.forIncident(incident.getType()), x + 6, y + 12, 22, 22);

        // ID y severidad
        gc.setFill(isSelected ? RetroTheme.YELLOW : RetroTheme.CREAM);
        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 11));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(incident.getId(), x + 32, y + 24);

        gc.setFill(sevColor);
        gc.setFont(Font.font("Consolas", FontWeight.NORMAL, 9));
        gc.fillText(incident.getSeverity().name(), x + 32, y + 37);

        drawNodes(gc, node.getLeft(), positions, nodeW, nodeH);
        drawNodes(gc, node.getRight(), positions, nodeW, nodeH);
    }

    private Color colorOf(Severity severity) {
        if (severity == Severity.HIGH) return RetroTheme.RED;
        if (severity == Severity.MEDIUM) return RetroTheme.ORANGE;
        return RetroTheme.GREEN;
    }

    @Override
    public boolean handleKey(KeyCode code) {
        if (code == KeyCode.DIGIT1) {
            setTraversalMode(TraversalMode.IN_ORDER);
            return true;
        }
        if (code == KeyCode.DIGIT2) {
            setTraversalMode(TraversalMode.PRE_ORDER);
            return true;
        }
        if (code == KeyCode.DIGIT3) {
            setTraversalMode(TraversalMode.POST_ORDER);
            return true;
        }
        return false;
    }

    public void setOnOpenRegistration(Runnable onOpenRegistration) {
        this.onOpenRegistration = onOpenRegistration;
    }
}
