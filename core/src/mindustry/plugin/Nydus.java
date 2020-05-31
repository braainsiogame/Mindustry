package mindustry.plugin;

import arc.func.*;
import mindustry.ui.*;

import static mindustry.Vars.*;

public enum Nydus{

    reactor_terrain_clearing(() -> true, "#thorium-reactor thorium reactor explosions {destroy terrain}"),

    pyratite_free_lead(() -> true, "#pyratite-mixer the pyratite mixer contains {free} #lead lead"),
    separator_free_slag(() -> true, "#separator separators contain {free} #slag slag"),
    cryofluid_free_water(() -> true, "#cryofluidmixer cryofluid is extracted from titanium {without} #water water"),

    separator_sedimentation(() -> true, "#separator separators that are {clogged} make #titanium titanium"),

    airblast_bonus_output(() -> true, "#blast-drill airblast drills periodically gives {5} bonus items"),
    landfill_on_drowning(() -> true, "#water drowning in deep water {while holding items} creates land"),
    enemy_item_drops(() -> true, "#core-shard enemies that die near your core give you {what they carry}"),

    pulverizer_on_ore(() -> true, "#pulverizer placing a pulverizer on any {ore} gives it infinite #scap scrap"),

    block_merging(() -> true, "#plastanium-wall-large several blocks {of the same type} might merge into one"),
    block_downgrading(() -> true, "#titanium-conveyor certain blocks {downgrade} instead of being destroyed #conveyor"),

    cascading_doors(() -> true, "#door using a door also toggles the doors {next to it}"),
    rtg_generator_sharing(() -> true, "#rtg-generator rtg generators {share} #thorium fuel with adjacent ones"),
    the_floor_is_lava(() -> true, "#magmarock magma tiles {set fire to anything} other than #thermal-generator thermal generators"),
    single_use_batteries(() -> true, "#battery batteries contain {a little power} when placed"),

    free_phase_overdrive(() -> true, "#overdrive-projector overdrivers get free #phase-fabric phase when {surrounded} by #phase-wall phase walls"),
    free_phase_forcefield(() -> true, "#overdrive-projector forcefields get free #phase-fabric phase when {surrounded} by #phase-wall phase walls"),
    free_thorium_weaver(() -> true, "#phase-weaver phase weavers get free #thorium thorium when {surrounded} by #thorium-wall thorium walls"),
    nuclear_demon_core(() -> true, "#thorium-reactor you can boost thorium reactors by {surrounding} them with #thorium-wall thorium walls"),


    duo_yeeter(() -> true, "#duo duo turrets can {lob} #copper-walls copper walls at the enemy"),
    repairpoint_wololoo(() -> true, "#repair-point getting close to an enemy repair point {switches your team}"),

    draug_home_isolation(() -> true, "#draug-factory miner drones work from home due to {corona}"),

    do_you_want_to_build_a_snowman(() -> true, "#rock rocks can only be moved by {ground mechs}"),
    launchpad_upgrading(() -> true, "#launch-pad a launch pad can {upgrade blocks automatically} for your team"),
    water_from_the_nile(() -> true, "#water {water from the nile} gives free water to some blocks"),
    game_of_doors(() -> true, "#door game of life in mindustry <3"),
    admin_aboose(() -> true, "#{admin} can run javascript");

    private final Boolp active;
    public String description;

    public boolean active(){
        if(!net.server()) return false;
        return active.get();
    }

    Nydus(Boolp active, String description){
        this.active = active;
        this.description = description;

        for(String word : description.split(" ")){
            if(word.startsWith("#")){
                this.description = this.description.replace(word, "" + (char)Fonts.getUnicode(word.substring(1)));
            }
        }

        this.description =this.description.replaceAll("\\{", "[accent]");
        this.description =this.description.replaceAll("\\}", "[]");
    }
}
