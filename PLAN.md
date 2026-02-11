# Mod information

we are using the ForgeModEnv from cleanroomMC (as were building against cleanroomMC 0.4.2)
so we must keep up with that.


## Configs / Data storing

config.json -- Main plugin data, has the master switch for each module & the command prefix (supports &a&l and etc)
Each module will have their own prefix, but it will be empty unless something gets put in it
modules/
    economy.json
    combat.json
    chat.json
    tab.json
    teleport.json

databases/
    data.sqlite


## Items

* Cocaine item [You can do a cp /home/styna/Downloads/cocaine.png to the needed location of it]

## Commands

Economy System:
    * /bal (user)
    * /baltop (user)
    Admin eco
        * /eco take
        * /eco give
        * /eco set
        
Discord Integration:
    * chat relay to discord [ Sends a embed for death, Advancements, Join/Leave, Server start/stop ]
    * linking account to discord [When the user does this, it will use their discord DISPLAY name, and pfp]

User:
    * /nick (op)
    * /back (costs money, configurable in teleport.json, 5 second delay displayed in actionbar)
    * home (2 for non ops, 10 for ops 5 second delay displayed in actionbar)
        /sethome
        /delhome
        /renamehomei
    * tpa (costs money, 150$ per tpa 5 second delay displayed in actionbar)
        /tpa <user>
        /tpahere <user>
    
    * /fly (will take xp if not op)
    * /speed (op)
    * /bossbar
        This will let us display a bossbar at the top of the screen for restarting


Combat log
    * Stops player from running any commands that teleport you. (will have a actionbar that displays how long ur in combat-log, its default 30s, red text)


API
    * a API for our economy system so that others can implement to it, we will make this in API.md in docs/