package com.slayspire.analyzer.database;

import com.slayspire.analyzer.models.Card;
import com.slayspire.analyzer.models.Relic;
import java.util.*;

public class StrategyBuilder {
    private List<Card> selectedCards;
    private List<Relic> selectedRelics;
    private Deque<BuildState> undoStack;
    private Deque<BuildState> redoStack;

    public StrategyBuilder() {
        selectedCards = new ArrayList<>();
        selectedRelics = new ArrayList<>();
        undoStack = new ArrayDeque<>();
        redoStack = new ArrayDeque<>();
    }

    public void addCard(Card card) {
        saveState();
        selectedCards.add(card);
        redoStack.clear();
    }

    public void removeCard(int index) {
        if (index >= 0 && index < selectedCards.size()) {
            saveState();
            selectedCards.remove(index);
            redoStack.clear();
        }
    }

    public void addRelic(Relic relic) {
        saveState();
        selectedRelics.add(relic);
        redoStack.clear();
    }

    public void removeRelic(int index) {
        if (index >= 0 && index < selectedRelics.size()) {
            saveState();
            selectedRelics.remove(index);
            redoStack.clear();
        }
    }

    public void clear() {
        saveState();
        selectedCards.clear();
        selectedRelics.clear();
        redoStack.clear();
    }

    public boolean undo() {
        if (undoStack.isEmpty()) return false;
        redoStack.push(new BuildState(selectedCards, selectedRelics));
        BuildState state = undoStack.pop();
        selectedCards = state.cards;
        selectedRelics = state.relics;
        return true;
    }

    public boolean redo() {
        if (redoStack.isEmpty()) return false;
        undoStack.push(new BuildState(selectedCards, selectedRelics));
        BuildState state = redoStack.pop();
        selectedCards = state.cards;
        selectedRelics = state.relics;
        return true;
    }

    private void saveState() {
        undoStack.push(new BuildState(selectedCards, selectedRelics));
        if (undoStack.size() > 50) {
            undoStack.removeLast();
        }
    }

    public List<Card> getSelectedCards() {
        return Collections.unmodifiableList(selectedCards);
    }

    public List<Relic> getSelectedRelics() {
        return Collections.unmodifiableList(selectedRelics);
    }

    public boolean canUndo() { return !undoStack.isEmpty(); }
    public boolean canRedo() { return !redoStack.isEmpty(); }

    static class BuildState {
        final List<Card> cards;
        final List<Relic> relics;

        BuildState(List<Card> cards, List<Relic> relics) {
            this.cards = new ArrayList<>(cards);
            this.relics = new ArrayList<>(relics);
        }
    }
}
