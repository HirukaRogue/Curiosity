the knowledge and knowledge recipe for jewelry are just examples.
You can replace them with your own knowledge and recipes as needed.
Or just delete them if you don't need them.
Make sure to update any references to these knowledge and recipes in your datapacks or mods accordingly.
Feel free to customize the content to better suit your project's theme and requirements!
Enjoy your modding!

loot table functions you can use for knowledge books:
setRandomKnowledge -> set a random amount of knowledge from the given tags
syntax:
{
    "function": "curiosity_researches:set_random_knowledge",
    "tags": <Your knowledge book tag list here>,
    "min": <Minimum number of knowledge to set>,
    "max": <Maximum number of knowledge to set>
    "custom_name": <Optional custom name for the knowledge book>
}
setKnowledge -> set all knowledge from the given tags
syntax:
{
    "function": "curiosity_researches:set_knowledge",
    "tags": <Your knowledge book tag list here>
    "custom_name": <Optional custom name for the knowledge book>
}


Non loot table stuffs
research notes to be used on researches:
With this you can add blocks, biomes, entities, items and dimensions to the research notes.
You can also add coordinates to the research notes.
syntax:
{
    "target": <The optional research target to add to the research notes, Resource location format>,
    "coordinates": {
        "x": <The x coordinate, optional>,
        "x2": <The x2 coordinate, optional>,
        "y": <The y coordinate, optional>,
        "y2": <The y2 coordinate, optional>.
        "z": <The z coordinate, optional>,
        "z2": <The z2 coordinate, optional>
    }
    "custom_name": <Optional custom name for the research note>
    "note": <The note text array to add to the research note>
}

Knowledge is the base for discoveries.
You can create your own knowledge by creating a json file in the data/curiosity_researches/knowledge folder.
Here is an example of a knowledge json file:
{
  "knowledge_name": "jewelry",
  "level": 1,
  "knowledge_description": ["The basics about jewelry crafting and materials."]
}

Unlocks defines what the knowledge unlocks.
syntax:
{
  "knowledge_name": <Knowledge Name>,
  "level": <Knowledge Level>,
  "unlocks": <Resource location array of what this knowledge unlocks>
}

To make a research parchment recipe, create a json file in the data/curiosity_researches/recipes folder.
syntax:
{
  "type": "curiosity_researches:research_parchment",
  "keys": {
    "<key>": {
      "component": "<item/tag>",
      "knowledge_key": "<<knowledge_name>_<level>, optional, only when the item is an incomplete research parchment>",
      "note_location": "<resource_location, optional, only when the item is a research note>",
      "count": <item count, optional>
      "consume": <true/false, optional, default: true>
    }
    ...
  },
    "pattern": [
        "<pattern row 1, size 2>",
        "<pattern row 2, size 3>",
        "<pattern row 3, size 2>"
    ],
    "knowledge": <Knowledge key string, syntax: "<knowledge_name>_<level>">,
    "tier": <Research tier, entries: incomplete, common, uncommon, rare, epic, legendary, MYTHIC>,
    "custom_name": <Optional custom name for the research parchment>,
    "paper_required": <quantity of paper required>
}