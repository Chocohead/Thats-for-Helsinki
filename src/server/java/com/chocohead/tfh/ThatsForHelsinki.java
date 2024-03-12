package com.chocohead.tfh;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.src.game.stats.StatBase;
import net.minecraft.src.game.stats.StatCrafting;
import net.minecraft.src.game.stats.StatList;

import com.fox2code.foxloader.loader.Mod;

public class ThatsForHelsinki extends Mod {
	@Override
	@SuppressWarnings("unchecked")
	public void onPostInit() {
		if (!Boolean.getBoolean("i-am-fingle-arnorsoneneson")) return;
		for (Pair<String, StatBase[]> pair : ArrayUtils.toArray(
				Pair.of("broken-blocks.txt", StatList.mineBlockStatArray), //Broken blocks
				Pair.of("created-things.txt", StatList.objectCraftStats), //Blocks/items crafted
				Pair.of("placed-used-things.txt", StatList.objectUseStats), //Placed blocks/used items
				Pair.of("worn-out-items.txt", StatList.objectBreakStats) //Worn out items
		)) {
			try (BufferedWriter out = Files.newBufferedWriter(Paths.get(pair.getLeft()))) {
				StatBase[] stats = pair.getRight();
				if (stats == null) {
					System.err.println(pair.getLeft() + " is null?");
					continue;
				}
				for (int i = 0; i < stats.length; i++) {
					StatBase stat = stats[i]; 
					if (stat != null) {
						out.write(Integer.toString(i));
						out.write('\t');
						out.write('\t');
						out.write(Integer.toString(stat.statId));
						out.write('\t');
						out.write('\t');
						out.write(stat.statName);
						out.newLine();
					}
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
		for (Pair<String, List<StatCrafting>> pair : ArrayUtils.toArray(
				Pair.of("item-stats.txt", StatList.field_25121_c),
				Pair.of("mined-blocks.txt", StatList.field_25120_d)
		)) {
			try (BufferedWriter out = Files.newBufferedWriter(Paths.get(pair.getLeft()))) {
				for (StatCrafting stat : pair.getRight()) {
					/*out.write(Integer.toString(stat.func_25072_b())); //Not retained server side
					out.write('\t');
					out.write('\t');*/
					out.write(Integer.toString(stat.statId));
					out.write('\t');
					out.write('\t');
					out.write(stat.statName);
					out.newLine();
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}
}