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