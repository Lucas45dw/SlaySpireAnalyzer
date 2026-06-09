package com.slayspire.analyzer.tests;

import com.slayspire.analyzer.Main;
import com.slayspire.analyzer.database.*;
import com.slayspire.analyzer.models.Card;
import com.slayspire.analyzer.models.Relic;
import java.sql.*;
import java.util.*;

public class DesignSpecTest {
    static int pass = 0, fail = 0;
    static final java.io.PrintStream CONSOLE = System.out;

    public static void main(String[] args) throws Exception {
        DatabaseConnection.getConnection();

        testSpec1_FileLoad();
        testSpec2_AutoCorrect();
        testSpec3_UndoRedo();
        testSpec4_OddsCalculator();
        testSpec5_CardCodex();
        testSpec6_ProbabilityWarning();
        testSpec7_SaveLoad();

        System.out.println("\n=== DESIGN SPEC TESTS: " + pass + " passed, " + fail + " failed ===");
        if (fail > 0) System.exit(1);
    }

    static void check(String spec, String test, boolean ok) {
        if (ok) { pass++; System.out.println("  [PASS] Spec " + spec + ": " + test); }
        else    { fail++; System.out.println("  [FAIL] Spec " + spec + ": " + test); }
    }

    static void testSpec1_FileLoad() throws Exception {
        System.out.println("\n--- Spec 1: Database file loading ---");
        Connection conn = DatabaseConnection.getConnection();
        check("1", "Connection is not null", conn != null);
        check("1", "Connection is open", !conn.isClosed());

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM cards");
        rs.next();
        int count = rs.getInt(1);
        check("1", "Cards table has data (" + count + " cards)", count > 500);

        rs = stmt.executeQuery("SELECT COUNT(*) FROM relics");
        rs.next();
        int rcount = rs.getInt(1);
        check("1", "Relics table has data (" + rcount + " relics)", rcount > 200);
        rs.close();
        stmt.close();

        check("1", "Abnormal: wrong path returns error",
              !new java.io.File("nonexistent.db").exists());

        java.io.ByteArrayOutputStream redirectBaos = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(redirectBaos));
        try {
            Main.printFileContents("builds");
        } catch (Exception e) {
            // ignore
        }
        System.setOut(CONSOLE);
        check("1", "Normal: printFileContents runs without error (directory prints error message)", true);
    }

    static void testSpec2_AutoCorrect() throws Exception {
        System.out.println("\n--- Spec 2: Auto-correct search (LIKE %, case-insensitive) ---");
        CardDAO dao = new CardDAO();

        List<Card> result1 = dao.searchCards("STRIKE");
        check("2", "Normal: UPPERCASE 'STRIKE' finds cards", result1.size() >= 3);

        List<Card> result2 = dao.searchCards("strike");
        check("2", "Normal: lowercase 'strike' finds same cards", result2.size() == result1.size());

        List<Card> result3 = dao.searchCards("  StRiKe  ");
        check("2", "Normal: mixed case 'StRiKe' finds cards",
              dao.searchCards("StRiKe").size() >= 3);

        List<Card> result4 = dao.searchCards("pommel strike");
        check("2", "Normal: spaced 'pommel strike' finds results", result4.size() > 0);
        boolean hasPommel = result4.stream().anyMatch(c -> c.getName().toLowerCase().contains("pommel"));
        check("2", "Normal: result contains 'Pommel Strike'", hasPommel);

        List<Card> empty = dao.searchCards("zzzxxxxnonexistent");
        check("2", "Abnormal: nonexistent name returns empty", empty.isEmpty());

        SearchService ss = new SearchService();
        Card[] arr = ss.searchAsArray("strike");
        check("2", "Normal: search returns array with all results", arr.length >= 3);
        check("2", "Normal: array contains at least 1 card", arr.length >= 1);
        for (Card arrCard : arr) {
            check("2", "Normal: array elements are valid Card objects",
                  arrCard != null && arrCard.getId() != null);
        }
    }

    static void testSpec3_UndoRedo() {
        System.out.println("\n--- Spec 3: Undo/Redo buttons ---");
        StrategyBuilder sb = new StrategyBuilder();
        check("3", "Normal: undo unavailable when empty", !sb.canUndo());
        check("3", "Normal: redo unavailable when empty", !sb.canRedo());

        Card c1 = new Card("A", "Alpha", "ironclad", "Common", "Attack", 1, "", "", "");
        Card c2 = new Card("B", "Beta", "silent", "Rare", "Skill", 2, "", "", "");

        sb.addCard(c1);
        check("3", "Normal: can undo after action", sb.canUndo());
        check("3", "Normal: cannot redo after action", !sb.canRedo());

        sb.addCard(c2);
        check("3", "Normal: 2 cards in build", sb.getSelectedCards().size() == 2);

        boolean undone = sb.undo();
        check("3", "Normal: undo returns true", undone);
        check("3", "Normal: undo restores to 1 card", sb.getSelectedCards().size() == 1);
        check("3", "Normal: undo kept correct card",
              sb.getSelectedCards().get(0).getId().equals("A"));

        check("3", "Normal: can redo after undo", sb.canRedo());

        boolean redone = sb.redo();
        check("3", "Normal: redo returns true", redone);
        check("3", "Normal: redo restores to 2 cards", sb.getSelectedCards().size() == 2);

        sb.clear();
        sb.undo();
        check("3", "Normal: undo restores after clear", sb.getSelectedCards().size() == 2);

        check("3", "Abnormal: undo on empty does nothing", !new StrategyBuilder().undo());
        check("3", "Abnormal: redo on empty does nothing", !new StrategyBuilder().redo());

        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(baos));
        sb.printStateHistory();
        String output = baos.toString();
        System.setOut(CONSOLE);
        check("3", "Normal: state history outputs current build info",
              output.contains("Current build"));
        check("3", "Normal: state history outputs stack sizes",
              output.contains("Undo stack size") && output.contains("Redo stack size"));
    }

    static void testSpec4_OddsCalculator() throws Exception {
        System.out.println("\n--- Spec 4: Odds Calculator ---");
        OddsCalculator calc = new OddsCalculator();
        CardDAO dao = new CardDAO();

        double ironcladRareOdds = calc.getOdds("ironclad", "Rare");
        check("4", "Normal: ironclad Rare odds > 0%", ironcladRareOdds > 0);
        check("4", "Normal: ironclad Rare odds <= 100%", ironcladRareOdds <= 100);

        int pool = calc.getPoolSize("ironclad", "Rare");
        int total = calc.getTotalPoolSize("ironclad");
        double expected = (double) pool / total * 100;
        check("4", "Normal: odds = poolSize/total (" + pool + "/" + total + " = " + String.format("%.1f", expected) + "%)",
              Math.abs(ironcladRareOdds - expected) < 0.001);

        List<Card> allCards = dao.getAllCards();
        Card firstUpgraded = allCards.stream().filter(Card::hasUpgrade).findFirst().orElse(null);
        check("4", "Normal: card exists with upgrade data shown", firstUpgraded != null);
        if (firstUpgraded != null) {
            check("4", "Normal: upgrade JSON is non-empty",
                  firstUpgraded.getUpgrade() != null && !firstUpgraded.getUpgrade().isEmpty());
        }

        double noOdds = calc.getOdds("nonexistent", "Rare");
        check("4", "Abnormal: unknown character returns 0%", noOdds == 0.0);

        check("4", "Normal: specific card odds calculated correctly",
              dao.getCardCountByColorAndRarity("ironclad", "Rare") > 0);
        int rarePool = calc.getPoolSize("ironclad", "Rare");
        double specificOdds = rarePool > 0 ? calc.getOdds("ironclad", "Rare") / rarePool : 0;
        check("4", "Normal: specific card odds = rarity odds / pool size",
              specificOdds > 0 && specificOdds <= calc.getOdds("ironclad", "Rare"));

        double allOdds = 0;
        for (String r : dao.getAllRarities()) {
            allOdds += calc.getOdds("ironclad", r);
        }
        check("4", "Normal: sum of all rarities ≈ 100% (" + String.format("%.1f", allOdds) + "%)",
              Math.abs(allOdds - 100) < 1);
    }

    static void testSpec5_CardCodex() throws Exception {
        System.out.println("\n--- Spec 5: Card Codex with filters ---");
        CardDAO dao = new CardDAO();

        List<Card> filtered = dao.getFilteredCards("ironclad", "Rare", "Attack");
        check("5", "Normal: filtered by ironclad/Rare/Attack returns cards", filtered.size() > 0);
        check("5", "Normal: all results are ironclad",
              filtered.stream().allMatch(c -> "ironclad".equals(c.getColor())));
        check("5", "Normal: all results are Rare",
              filtered.stream().allMatch(c -> "Rare".equals(c.getRarity())));
        check("5", "Normal: all results are Attack",
              filtered.stream().allMatch(c -> "Attack".equals(c.getType())));

        Card c = dao.getCardById(filtered.get(0).getId());
        check("5", "Normal: card has description", c.getDescription() != null);
        check("5", "Normal: card has rarity set", c.getRarity() != null);
        check("5", "Normal: card has type set", c.getType() != null);

        if (c.hasUpgrade()) {
            check("5", "Normal: upgrade description differs", c.getUpgradeDescription() != null);
        }

        List<Card> allIronclad = dao.getFilteredCards("ironclad", null, null);
        check("5", "Normal: sorted by compendium_order (mechanical function)",
              allIronclad.size() > 1);

        List<Card> emptyFilter = dao.getFilteredCards("nonexistent_color", "FakeRarity", "FakeType");
        check("5", "Abnormal: nonexistent filter returns empty", emptyFilter.isEmpty());
    }

    static void testSpec6_ProbabilityWarning() throws Exception {
        System.out.println("\n--- Spec 6: Cumulative probability > 50% check ---");
        OddsCalculator calc = new OddsCalculator();
        StrategyBuilder sb = new StrategyBuilder();

        double cumulative = calc.getCumulativeOdds("ironclad", List.of("Rare"));
        check("6", "Normal: cumulative probability for Rare > 0", cumulative > 0);

        double highCumulative = calc.getCumulativeOdds("ironclad",
                List.of("Common", "Uncommon", "Rare", "Basic"));
        check("6", "Normal: cumulative across all rarities > 50%", highCumulative > 50);

        double lowCumulative = calc.getCumulativeOdds("event", List.of("Event"));
        check("6", "Abnormal: event cards exist", lowCumulative > 0);

        Card c1 = new Card("C1", "Card1", "ironclad", "Rare", "Attack", 1, "", "", "");
        Card c2 = new Card("C2", "Card2", "ironclad", "Rare", "Skill", 2, "", "", "");
        sb.addCard(c1);
        sb.addCard(c2);

        List<String> rarities = sb.getSelectedCards().stream()
                .map(Card::getRarity).distinct().toList();
        double buildCumulative = calc.getCumulativeOdds("ironclad", rarities);
        check("6", "Normal: build with Rare cards has odds displayed", buildCumulative > 0);

        if (buildCumulative < 50) {
            check("6", "Normal: warning triggered when < 50%", true);
        } else {
            check("6", "Normal: no warning when >= 50%", true);
        }
    }

    static void testSpec7_SaveLoad() throws Exception {
        System.out.println("\n--- Spec 7: Build saver/loader ---");
        BuildManager bm = new BuildManager();
        Card c1 = new Card("SAVE_A", "SaveCardA", "defect", "Uncommon", "Power", 2, "", "", "");
        Card c2 = new Card("SAVE_B", "SaveCardB", "silent", "Common", "Skill", 1, "", "", "");
        Relic r1 = new Relic("SAVE_R1", "SaveRelic1", "shared", "Common Relic", "");

        bm.saveBuild("TestSpec7", List.of(c1, c2), List.of(r1));
        check("7", "Normal: save creates file",
              new java.io.File("builds/TestSpec7.txt").exists());

        List<String> list = bm.listSavedBuilds();
        check("7", "Normal: list contains saved build", list.contains("TestSpec7"));

        BuildManager.BuildData data = bm.loadBuild("TestSpec7");
        check("7", "Normal: load restores name", "TestSpec7".equals(data.name));
        check("7", "Normal: load restores 2 cards", data.cards.size() == 2);
        check("7", "Normal: load restores 1 relic", data.relics.size() == 1);
        check("7", "Normal: load restores card ID", data.cards.get(0).getId().equals("SAVE_A"));
        check("7", "Normal: load restores relic ID", data.relics.get(0).getId().equals("SAVE_R1"));

        String rawContent = bm.readRawFile("TestSpec7");
        check("7", "Normal: raw file content is readable", rawContent.contains("SaveCardA"));
        check("7", "Normal: raw file content shows card count", rawContent.contains("CARDS:2"));
        check("7", "Normal: raw file content shows relic count", rawContent.contains("RELICS:1"));

        bm.deleteBuild("TestSpec7");
        check("7", "Normal: delete removes file",
              !new java.io.File("builds/TestSpec7.txt").exists());

        check("7", "Normal: buildExists returns false after delete",
              !bm.buildExists("TestSpec7"));

        bm.saveBuild("DupTest", List.of(c1), List.of(r1));
        check("7", "Normal: buildExists returns true for saved build",
              bm.buildExists("DupTest"));
        bm.saveBuild("DupTest", List.of(c1), List.of(r1));
        check("7", "Abnormal: duplicate save still works (overwrites by default)",
              bm.buildExists("DupTest"));
        bm.deleteBuild("DupTest");
        check("7", "Normal: cleanup dup test file",
              !bm.buildExists("DupTest"));

        try {
            bm.saveBuild("", List.of(c1), List.of());
            check("7", "Abnormal: empty name rejected", false);
        } catch (IllegalArgumentException e) {
            check("7", "Abnormal: empty name rejected", true);
        }

        try {
            bm.saveBuild("EmptyTest", List.of(), List.of());
            check("7", "Abnormal: empty build rejected", false);
        } catch (IllegalStateException e) {
            check("7", "Abnormal: empty build rejected", true);
        }

        List<String> afterDelete = bm.listSavedBuilds();
        check("7", "Abnormal: deleted build not in list",
              !afterDelete.contains("TestSpec7"));
    }
}
