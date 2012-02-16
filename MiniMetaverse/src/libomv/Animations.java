/*
 * Copyright (c) 2007-2008, openmetaverse.org
 * All rights reserved.
 *
 * - Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Neither the name of the openmetaverse.org nor the names
 *   of its contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package libomv;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import libomv.types.UUID;

// Static pre-defined animations available to all agents
public class Animations
{
    // Agent with afraid expression on face
    public final static UUID AFRAID = new UUID("6b61c8e8-4747-0d75-12d7-e49ff207a4ca");
    // Agent aiming a bazooka (right handed)
    public final static UUID AIM_BAZOOKA_R = new UUID("b5b4a67d-0aee-30d2-72cd-77b333e932ef");
    // Agent aiming a bow (left handed)
    public final static UUID AIM_BOW_L = new UUID("46bb4359-de38-4ed8-6a22-f1f52fe8f506");
    // Agent aiming a hand gun (right handed)
    public final static UUID AIM_HANDGUN_R = new UUID("3147d815-6338-b932-f011-16b56d9ac18b");
    // Agent aiming a rifle (right handed)
    public final static UUID AIM_RIFLE_R = new UUID("ea633413-8006-180a-c3ba-96dd1d756720");
    // Agent with angry expression on face
    public final static UUID ANGRY = new UUID("5747a48e-073e-c331-f6f3-7c2149613d3e");
    // Agent hunched over (away)
    public final static UUID AWAY = new UUID("fd037134-85d4-f241-72c6-4f42164fedee");
    // Agent doing a backflip
    public final static UUID BACKFLIP = new UUID("c4ca6188-9127-4f31-0158-23c4e2f93304");
    // Agent laughing while holding belly
    public final static UUID BELLY_LAUGH = new UUID("18b3a4b5-b463-bd48-e4b6-71eaac76c515");
    // Agent blowing a kiss
    public final static UUID BLOW_KISS = new UUID("db84829b-462c-ee83-1e27-9bbee66bd624");
    // Agent with bored expression on face
    public final static UUID BORED = new UUID("b906c4ba-703b-1940-32a3-0c7f7d791510");
    // Agent bowing to audience
    public final static UUID BOW = new UUID("82e99230-c906-1403-4d9c-3889dd98daba");
    // Agent brushing himself/herself off
    public final static UUID BRUSH = new UUID("349a3801-54f9-bf2c-3bd0-1ac89772af01");
    // Agent in busy mode
    public final static UUID BUSY = new UUID("efcf670c-2d18-8128-973a-034ebc806b67");
    // Agent clapping hands
    public final static UUID CLAP = new UUID("9b0c1c4e-8ac7-7969-1494-28c874c4f668");
    // Agent doing a curtsey bow
    public final static UUID COURTBOW = new UUID("9ba1c942-08be-e43a-fb29-16ad440efc50");
    // Agent crouching
    public final static UUID CROUCH = new UUID("201f3fdf-cb1f-dbec-201f-7333e328ae7c");
    // Agent crouching while walking
    public final static UUID CROUCHWALK = new UUID("47f5f6fb-22e5-ae44-f871-73aaaf4a6022");
    // Agent crying
    public final static UUID CRY = new UUID("92624d3e-1068-f1aa-a5ec-8244585193ed");
    // Agent unanimated with arms out (e.g. setting appearance)
    public final static UUID CUSTOMIZE = new UUID("038fcec9-5ebd-8a8e-0e2e-6e71a0a1ac53");
    // Agent re-animated after set appearance finished
    public final static UUID CUSTOMIZE_DONE = new UUID("6883a61a-b27b-5914-a61e-dda118a9ee2c");
    // Agent dancing
    public final static UUID DANCE1 = new UUID("b68a3d7c-de9e-fc87-eec8-543d787e5b0d");
    // Agent dancing
    public final static UUID DANCE2 = new UUID("928cae18-e31d-76fd-9cc9-2f55160ff818");
    // Agent dancing
    public final static UUID DANCE3 = new UUID("30047778-10ea-1af7-6881-4db7a3a5a114");
    // Agent dancing
    public final static UUID DANCE4 = new UUID("951469f4-c7b2-c818-9dee-ad7eea8c30b7");
    // Agent dancing
    public final static UUID DANCE5 = new UUID("4bd69a1d-1114-a0b4-625f-84e0a5237155");
    // Agent dancing
    public final static UUID DANCE6 = new UUID("cd28b69b-9c95-bb78-3f94-8d605ff1bb12");
    // Agent dancing
    public final static UUID DANCE7 = new UUID("a54d8ee2-28bb-80a9-7f0c-7afbbe24a5d6");
    // Agent dancing
    public final static UUID DANCE8 = new UUID("b0dc417c-1f11-af36-2e80-7e7489fa7cdc");
    // Agent on ground unanimated
    public final static UUID DEAD = new UUID("57abaae6-1d17-7b1b-5f98-6d11a6411276");
    // Agent boozing it up
    public final static UUID DRINK = new UUID("0f86e355-dd31-a61c-fdb0-3a96b9aad05f");
    // Agent with embarassed expression on face
    public final static UUID EMBARRASSED = new UUID("514af488-9051-044a-b3fc-d4dbf76377c6");
    // Agent with afraid expression on face
    public final static UUID EXPRESS_AFRAID = new UUID("aa2df84d-cf8f-7218-527b-424a52de766e");
    // Agent with angry expression on face
    public final static UUID EXPRESS_ANGER = new UUID("1a03b575-9634-b62a-5767-3a679e81f4de");
    // Agent with bored expression on face
    public final static UUID EXPRESS_BORED = new UUID("214aa6c1-ba6a-4578-f27c-ce7688f61d0d");
    // Agent crying
    public final static UUID EXPRESS_CRY = new UUID("d535471b-85bf-3b4d-a542-93bea4f59d33");
    // Agent showing disdain (dislike) for something
    public final static UUID EXPRESS_DISDAIN = new UUID("d4416ff1-09d3-300f-4183-1b68a19b9fc1");
    // Agent with embarassed expression on face
    public final static UUID EXPRESS_EMBARRASSED = new UUID("0b8c8211-d78c-33e8-fa28-c51a9594e424");
    // Agent with frowning expression on face
    public final static UUID EXPRESS_FROWN = new UUID("fee3df48-fa3d-1015-1e26-a205810e3001");
    // Agent with kissy face
    public final static UUID EXPRESS_KISS = new UUID("1e8d90cc-a84e-e135-884c-7c82c8b03a14");
    // Agent expressing laughgter
    public final static UUID EXPRESS_LAUGH = new UUID("62570842-0950-96f8-341c-809e65110823");
    // Agent with open mouth
    public final static UUID EXPRESS_OPEN_MOUTH = new UUID("d63bc1f9-fc81-9625-a0c6-007176d82eb7");
    // Agent with repulsed expression on face
    public final static UUID EXPRESS_REPULSED = new UUID("f76cda94-41d4-a229-2872-e0296e58afe1");
    // Agent expressing sadness
    public final static UUID EXPRESS_SAD = new UUID("eb6ebfb2-a4b3-a19c-d388-4dd5c03823f7");
    // Agent shrugging shoulders
    public final static UUID EXPRESS_SHRUG = new UUID("a351b1bc-cc94-aac2-7bea-a7e6ebad15ef");
    // Agent with a smile
    public final static UUID EXPRESS_SMILE = new UUID("b7c7c833-e3d3-c4e3-9fc0-131237446312");
    // Agent expressing surprise
    public final static UUID EXPRESS_SURPRISE = new UUID("728646d9-cc79-08b2-32d6-937f0a835c24");
    // Agent sticking tongue out
    public final static UUID EXPRESS_TONGUE_OUT = new UUID("835965c6-7f2f-bda2-5deb-2478737f91bf");
    // Agent with big toothy smile
    public final static UUID EXPRESS_TOOTHSMILE = new UUID("b92ec1a5-e7ce-a76b-2b05-bcdb9311417e");
    // Agent winking
    public final static UUID EXPRESS_WINK = new UUID("da020525-4d94-59d6-23d7-81fdebf33148");
    // Agent expressing worry
    public final static UUID EXPRESS_WORRY = new UUID("9c05e5c7-6f07-6ca4-ed5a-b230390c3950");
    // Agent falling down
    public final static UUID FALLDOWN = new UUID("666307d9-a860-572d-6fd4-c3ab8865c094");
    // Agent walking (feminine version)
    public final static UUID FEMALE_WALK = new UUID("f5fc7433-043d-e819-8298-f519a119b688");
    // Agent wagging finger (disapproval)
    public final static UUID FINGER_WAG = new UUID("c1bc7f36-3ba0-d844-f93c-93be945d644f");
    // I'm not sure I want to know
    public final static UUID FIST_PUMP = new UUID("7db00ccd-f380-f3ee-439d-61968ec69c8a");
    // Agent in superman position
    public final static UUID FLY = new UUID("aec4610c-757f-bc4e-c092-c6e9caf18daf");
    // Agent in superman position
    public final static UUID FLYSLOW = new UUID("2b5a38b2-5e00-3a97-a495-4c826bc443e6");
    // Agent greeting another
    public final static UUID HELLO = new UUID("9b29cd61-c45b-5689-ded2-91756b8d76a9");
    // Agent holding bazooka (right handed)
    public final static UUID HOLD_BAZOOKA_R = new UUID("ef62d355-c815-4816-2474-b1acc21094a6");
    // Agent holding a bow (left handed)
    public final static UUID HOLD_BOW_L = new UUID("8b102617-bcba-037b-86c1-b76219f90c88");
    // Agent holding a handgun (right handed)
    public final static UUID HOLD_HANDGUN_R = new UUID("efdc1727-8b8a-c800-4077-975fc27ee2f2");
    // Agent holding a rifle (right handed)
    public final static UUID HOLD_RIFLE_R = new UUID("3d94bad0-c55b-7dcc-8763-033c59405d33");
    // Agent throwing an object (right handed)
    public final static UUID HOLD_THROW_R = new UUID("7570c7b5-1f22-56dd-56ef-a9168241bbb6");
    // Agent in static hover
    public final static UUID HOVER = new UUID("4ae8016b-31b9-03bb-c401-b1ea941db41d");
    // Agent hovering downward
    public final static UUID HOVER_DOWN = new UUID("20f063ea-8306-2562-0b07-5c853b37b31e");
    // Agent hovering upward
    public final static UUID HOVER_UP = new UUID("62c5de58-cb33-5743-3d07-9e4cd4352864");
    // Agent being impatient
    public final static UUID IMPATIENT = new UUID("5ea3991f-c293-392e-6860-91dfa01278a3");
    // Agent jumping
    public final static UUID JUMP = new UUID("2305bd75-1ca9-b03b-1faa-b176b8a8c49e");
    // Agent jumping with fervor
    public final static UUID JUMP_FOR_JOY = new UUID("709ea28e-1573-c023-8bf8-520c8bc637fa");
    // Agent point to lips then rear end
    public final static UUID KISS_MY_BUTT = new UUID("19999406-3a3a-d58c-a2ac-d72e555dcf51");
    // Agent landing from jump, finished flight, etc
    public final static UUID LAND = new UUID("7a17b059-12b2-41b1-570a-186368b6aa6f");
    // Agent laughing
    public final static UUID LAUGH_SHORT = new UUID("ca5b3f14-3194-7a2b-c894-aa699b718d1f");
    // Agent landing from jump, finished flight, etc
    public final static UUID MEDIUM_LAND = new UUID("f4f00d6e-b9fe-9292-f4cb-0ae06ea58d57");
    // Agent sitting on a motorcycle
    public final static UUID MOTORCYCLE_SIT = new UUID("08464f78-3a8e-2944-cba5-0c94aff3af29");
    // 
    public final static UUID MUSCLE_BEACH = new UUID("315c3a41-a5f3-0ba4-27da-f893f769e69b");
    // Agent moving head side to side
    public final static UUID NO = new UUID("5a977ed9-7f72-44e9-4c4c-6e913df8ae74");
    // Agent moving head side to side with unhappy expression
    public final static UUID NO_UNHAPPY = new UUID("d83fa0e5-97ed-7eb2-e798-7bd006215cb4");
    // Agent taunting another
    public final static UUID NYAH_NYAH = new UUID("f061723d-0a18-754f-66ee-29a44795a32f");
    // 
    public final static UUID ONETWO_PUNCH = new UUID("eefc79be-daae-a239-8c04-890f5d23654a");
    // Agent giving peace sign
    public final static UUID PEACE = new UUID("b312b10e-65ab-a0a4-8b3c-1326ea8e3ed9");
    // Agent pointing at self
    public final static UUID POINT_ME = new UUID("17c024cc-eef2-f6a0-3527-9869876d7752");
    // Agent pointing at another
    public final static UUID POINT_YOU = new UUID("ec952cca-61ef-aa3b-2789-4d1344f016de");
    // Agent preparing for jump (bending knees)
    public final static UUID PRE_JUMP = new UUID("7a4e87fe-de39-6fcb-6223-024b00893244");
    // Agent punching with left hand
    public final static UUID PUNCH_LEFT = new UUID("f3300ad9-3462-1d07-2044-0fef80062da0");
    // Agent punching with right hand
    public final static UUID PUNCH_RIGHT = new UUID("c8e42d32-7310-6906-c903-cab5d4a34656");
    // Agent acting repulsed
    public final static UUID REPULSED = new UUID("36f81a92-f076-5893-dc4b-7c3795e487cf");
    // Agent trying to be Chuck Norris
    public final static UUID ROUNDHOUSE_KICK = new UUID("49aea43b-5ac3-8a44-b595-96100af0beda");
    // Rocks, Paper, Scissors 1, 2, 3
    public final static UUID RPS_COUNTDOWN = new UUID("35db4f7e-28c2-6679-cea9-3ee108f7fc7f");
    // Agent with hand flat over other hand
    public final static UUID RPS_PAPER = new UUID("0836b67f-7f7b-f37b-c00a-460dc1521f5a");
    // Agent with fist over other hand
    public final static UUID RPS_ROCK = new UUID("42dd95d5-0bc6-6392-f650-777304946c0f");
    // Agent with two fingers spread over other hand
    public final static UUID RPS_SCISSORS = new UUID("16803a9f-5140-e042-4d7b-d28ba247c325");
    // Agent running
    public final static UUID RUN = new UUID("05ddbff8-aaa9-92a1-2b74-8fe77a29b445");
    // Agent appearing sad
    public final static UUID SAD = new UUID("0eb702e2-cc5a-9a88-56a5-661a55c0676a");
    // Agent saluting
    public final static UUID SALUTE = new UUID("cd7668a6-7011-d7e2-ead8-fc69eff1a104");
    // Agent shooting bow (left handed)
    public final static UUID SHOOT_BOW_L = new UUID("e04d450d-fdb5-0432-fd68-818aaf5935f8");
    // Agent cupping mouth as if shouting
    public final static UUID SHOUT = new UUID("6bd01860-4ebd-127a-bb3d-d1427e8e0c42");
    // Agent shrugging shoulders
    public final static UUID SHRUG = new UUID("70ea714f-3a97-d742-1b01-590a8fcd1db5");
    // Agent in sit position
    public final static UUID SIT = new UUID("1a5fe8ac-a804-8a5d-7cbd-56bd83184568");
    // Agent in sit position (feminine)
    public final static UUID SIT_FEMALE = new UUID("b1709c8d-ecd3-54a1-4f28-d55ac0840782");
    // Agent in sit position (generic)
    public final static UUID SIT_GENERIC = new UUID("245f3c54-f1c0-bf2e-811f-46d8eeb386e7");
    // Agent sitting on ground
    public final static UUID SIT_GROUND = new UUID("1c7600d6-661f-b87b-efe2-d7421eb93c86");
    // Agent sitting on ground
    public final static UUID SIT_GROUND_staticRAINED = new UUID("1a2bd58e-87ff-0df8-0b4c-53e047b0bb6e");
    // 
    public final static UUID SIT_TO_STAND = new UUID("a8dee56f-2eae-9e7a-05a2-6fb92b97e21e");
    // Agent sleeping on side
    public final static UUID SLEEP = new UUID("f2bed5f9-9d44-39af-b0cd-257b2a17fe40");
    // Agent smoking
    public final static UUID SMOKE_IDLE = new UUID("d2f2ee58-8ad1-06c9-d8d3-3827ba31567a");
    // Agent inhaling smoke
    public final static UUID SMOKE_INHALE = new UUID("6802d553-49da-0778-9f85-1599a2266526");
    // 
    public final static UUID SMOKE_THROW_DOWN = new UUID("0a9fb970-8b44-9114-d3a9-bf69cfe804d6");
    // Agent taking a picture
    public final static UUID SNAPSHOT = new UUID("eae8905b-271a-99e2-4c0e-31106afd100c");
    // Agent standing
    public final static UUID STAND = new UUID("2408fe9e-df1d-1d7d-f4ff-1384fa7b350f");
    // Agent standing up
    public final static UUID STANDUP = new UUID("3da1d753-028a-5446-24f3-9c9b856d9422");
    // Agent standing
    public final static UUID STAND_1 = new UUID("15468e00-3400-bb66-cecc-646d7c14458e");
    // Agent standing
    public final static UUID STAND_2 = new UUID("370f3a20-6ca6-9971-848c-9a01bc42ae3c");
    // Agent standing
    public final static UUID STAND_3 = new UUID("42b46214-4b44-79ae-deb8-0df61424ff4b");
    // Agent standing
    public final static UUID STAND_4 = new UUID("f22fed8b-a5ed-2c93-64d5-bdd8b93c889f");
    // Agent stretching
    public final static UUID STRETCH = new UUID("80700431-74ec-a008-14f8-77575e73693f");
    // Agent in stride (fast walk)
    public final static UUID STRIDE = new UUID("1cb562b0-ba21-2202-efb3-30f82cdf9595");
    // Agent surfing
    public final static UUID SURF = new UUID("41426836-7437-7e89-025d-0aa4d10f1d69");
    // Agent acting surprised
    public final static UUID SURPRISE = new UUID("313b9881-4302-73c0-c7d0-0e7a36b6c224");
    // Agent striking with a sword
    public final static UUID SWORD_STRIKE = new UUID("85428680-6bf9-3e64-b489-6f81087c24bd");
    // Agent talking (lips moving)
    public final static UUID TALK = new UUID("5c682a95-6da4-a463-0bf6-0f5b7be129d1");
    // Agent throwing a tantrum
    public final static UUID TANTRUM = new UUID("11000694-3f41-adc2-606b-eee1d66f3724");
    // Agent throwing an object (right handed)
    public final static UUID THROW_R = new UUID("aa134404-7dac-7aca-2cba-435f9db875ca");
    // Agent trying on a shirt
    public final static UUID TRYON_SHIRT = new UUID("83ff59fe-2346-f236-9009-4e3608af64c1");
    // Agent turning to the left
    public final static UUID TURNLEFT = new UUID("56e0ba0d-4a9f-7f27-6117-32f2ebbf6135");
    // Agent turning to the right
    public final static UUID TURNRIGHT = new UUID("2d6daa51-3192-6794-8e2e-a15f8338ec30");
    // Agent typing
    public final static UUID TYPE = new UUID("c541c47f-e0c0-058b-ad1a-d6ae3a4584d9");
    // Agent walking
    public final static UUID WALK = new UUID("6ed24bd8-91aa-4b12-ccc7-c97c857ab4e0");
    // Agent whispering
    public final static UUID WHISPER = new UUID("7693f268-06c7-ea71-fa21-2b30d6533f8f");
    // Agent whispering with fingers in mouth
    public final static UUID WHISTLE = new UUID("b1ed7982-c68e-a982-7561-52a88a5298c0");
    // Agent winking
    public final static UUID WINK = new UUID("869ecdad-a44b-671e-3266-56aef2e3ac2e");
    // Agent winking
    public final static UUID WINK_HOLLYWOOD = new UUID("c0c4030f-c02b-49de-24ba-2331f43fe41c");
    // Agent worried
    public final static UUID WORRY = new UUID("9f496bd2-589a-709f-16cc-69bf7df1d36c");
    // Agent nodding yes
    public final static UUID YES = new UUID("15dd911d-be82-2856-26db-27659b142875");
    // Agent nodding yes with happy face
    public final static UUID YES_HAPPY = new UUID("b8c8b2a3-9008-1771-3bfc-90924955ab2d");
    // Agent floating with legs and arms crossed
    public final static UUID YOGA_FLOAT = new UUID("42ecd00b-9947-a97c-400a-bbc9174c7aeb");

    /**
     * A dictionary containing all pre-defined animations
     * 
     * @returns A dictionary containing the pre-defined animations, where the key is the animations ID,
     * and the value is a string containing a name to identify the purpose of the animation
     */
    
    private static HashMap<UUID, String> dict = null;
    
    public HashMap<UUID, String> toDictionary()
    {
    	if (dict == null)
    	{
    		dict = new HashMap<UUID, String>();
    		java.lang.Class<?> type = Animations.class;
    		for (Field field : type.getDeclaredFields())
    		{
    			int flags = field.getModifiers();
    			if (Modifier.isPublic(flags) || Modifier.isStatic(flags))
    			{
    				try
					{
						dict.put((UUID)field.get(this), field.getName());
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
    			}
    		}
    	}
        return dict;
    }
}
