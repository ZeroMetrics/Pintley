package com.example.pintly

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.*
import java.util.LinkedList
import kotlin.collections.ArrayList

data class MessageGroup(val name: String, var messages: LinkedList<String>, val weight: Int)

class MainActivity : AppCompatActivity() {
    private val random = Random()
    private lateinit var tileTypeTextView: TextView

    // Get player names from Intent
    private val playerNames by lazy {
        intent.getStringArrayListExtra("names") ?: arrayListOf<String>()
    }

    private fun showMessage(message: String) {
        // Display the message in a dialog or toast, or any other UI element of your choice
        // For example, you can show it in a toast:
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private lateinit var messageGroups: List<MessageGroup>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Pre-calculate and store the random messages and players.
        val preparedMessageGroups = mutableListOf(
            MessageGroup(
                "Regular",
                LinkedList<String>().apply {
                    add("Take a drink if you have ever crashed your car")
                    add("Take a drink for every book you read this year. If you answer 0, down your drink - illiterate")
                    add("Player X share a lie that you told someone in the group and they believed, if you can't then drink 2")
                    add("Take a drink if you have slept with someone in the group, don't explain")
                    add("Whoever is the least drunk, drink 3")
                    add("Everyone takes a drink")
                    add("Take a sip for every country you have visited")
                    add("Player X, make older players drink 2, if you are the oldest drink 3 for being such a creep")
                    add("Player X, make younger players drink 2, if you are the youngest drink 3 and blink twice for help")
                    add("Player X, the group asks you one question, if you don't want to answer drink a shot")
                    add("Player X, choose a category (eg cars), if someone can't continue the category within 5 seconds they drink 3")
                    add("Player X, take 2 drinks")
                    add("All women drink 1, if there's no women here everyone drinks 2 in shame")
                    add("Person with the largest feet drinks 2")
                    add("Player X, anyone with the same first letter drinks 1, yes that includes you")
                    add("Player X, give out 2 drinks to whoever you choose")
                    add("Person closest to the door drinks 2")
                    add("Everyone take 2 drinks, if your drink has ice in it take 4 for being precious")
                    add("People in a relationship, drink 2")
                    add("Drink if you haven't been to the gym in the last month, drink double if you have a membership")
                    add("Drink for each streaming service you are subscribed to")
                    add("If you are romantically interested in someone here, take a drink but don't explain yourself")
                    add("If you are under 6'0 drink 2 in shame, manlet")
                    add("Drink 3 if you have ever complained about food at a restaurant")
                    add("Player X, give out as many drinks as first names you have")
                    add("BOOM! Player X down your drink!")
                    add("Drink 3 if you have given an excuse to why you won't drink today")
                    add("Drink 2 times if you can't sleep after sex, virgins drink 3 in sadness")
                    add("Women drink your bra size, A = 1, B = 2, etc")
                    add("Drink 2 if you have said I love you in the last 24 hours")
                    add("Drink 3 if you have ever painted your nails")
                    add("Men, 1 drink per inch. Please lie for God's sake")
                    add("Everyone take a drink for each piece of black clothing you are wearing, very boring")
                    add("Player X, give out 3 sips to a player smaller than you, if you can't - drink them!")
                    add("If you have beautiful hair, drink 3")
                    add("If you have short hair, drink 2 drinks")
                    add("If you have blond hair, give out 3 drinks")
                    add("Most highly educated drinks 2, know it all")
                    add("Drink 2 if you have ever shoplifted")
                    add("Drink 1 if you can play an instrument")
                    add("Drink 1 if you drove here")
                    add("Drink 1 if you've been on a bus in the last month")
                    add("The last person who drank, drink that amount again")
                    add("Whoever knows the least people here drinks")
                    add("The host drinks, round of applause please")
                    add("Whoever lives furthest from here, drinks 2")
                    add("Whoever poured their drink longest ago, finally finish it off")
                    add("Player X take 3 drinks of Player Y's drink")
                    add("Drink once for each blue eye")
                    add("Papers please! Drink 1 if you don't have ID on you")
                    add("Anyone under 5'6 or with a beard, drink 3 like a true dwarf! 6 if both")
                    add("Whoever has met the most famous person gives out 4 sips")
                    add("Person with the most photos on their phone drinks 1")
                    add("Whoever's phone this is, give out 5 drinks for supporting my app :)")
                    add("Anyone wearing a watch, take 1 drink")
                    add("Everyone with a Samsung drink 1")
                    add("Drink 2 if you shaved today")
                    add("Player X what's the name of the song and artist playing right now. Drink 3 if you are incorrect")
                    add("Those who drank for the previous tile, drink again")
                    add("Everyone take as many sips as you studied after school")
                    add("Player X, give out 3 drinks to a player who is less drunk than you - if its impossible down 6 drinks")
                    add("Player X, if you are wearing clothes you bought this month, take 3 drinks")
                    add("Player X, if you don't have Player Y then you can both drink 1")
                    add("If you believe in ghosts, drink 3")
                    add("If you have studied more than Player X, drink 2")
                    add("Drink 2 if you've slept with someone in the room. Drink 5 if you feel the need to explain")
                    add("Largest player, give out 2 sips")
                    add("Drink 1 if you have ever skinny dipped")
                    add("Take a drink if you have ever eaten an insect")
                    add("Take a drink if you \"Don't dance\"")
                    add("Take 2 drinks if you are unemployed, 4 if you have been for over a year")
                    add("Take 2 drinks if you have had a celebrity encounter")
                    add("Player X, drink 3 drinks if you have ever gone to a costume party")
                    add("Player X, take 3 drink if you have never been to a casino. Very boring!")
                    add("If you think you can dance, give out 2 drinks")
                    add("Take 2 drinks if you have never been in a fight")
                    add("Take 1 drink if you have ever played twister, 3 if it was sexy twister")
                    add("Give out 1 shot if you rated this app 5 stars, I love you")
                    add("Give out 2 drinks if you usually play a sport, take 4 if you're bad at it")
                    add("Take 2 drinks if you have ever used a dating app, honestly that's depressing")
                    add("Give 2 drinks if you have been to a music festival")
                    add("Player X, donate a shot if you have volunteered for a charity")
                    add("Drink 3 if you're a virgin, double it if you study computer science")
                    add("Bodybuilders drink 3, you can take it")
                    add("Player X, if you can touch your nose with your tongue, give out 2")
                    add("Player X take a drink for every social media app on your phone")
                    add("Drink 1 if you have had a sexual fantasy about someone in the room. Don't explain")
                    add("Player X, drink 2 if you lied on your resume")
                    add("If you arrived alone drink 1. If you think you'll leave alone, drink 2")
                    add("If you know what League of Legends is, drink 2")
                    add("Player X, name a TV show. Whoever has seen it drink 1")
                    add("Drink 1 if you have signed a petition this year")
                    add("Drink 1 if you like pineapple on pizza")
                    add("Drink 2 if you ever tried to stream on twitch")
                    add("Drink 1 if you don't want kids, give out 2 drinks if you do")
                    add("Player X, name a movie. Whoever has seen it drink 1")
                    add("Give out 3 drinks if you have ever been arrested")
                    add("Give out 2 drinks if you have hugged a dangerous animal such as a lion or tiger")
                    add("Drink 2 if you have more than 5 GCSEs (or equivalent)")
                    add("Player X drink for every tattoo you have, if you have none then give out 2 drinks")
                    add("Drink 3 if you have slept with an ex, double it if it's a friends ex")
                    add("If you are over 18 and still use snapchat take a drink")
                    add("Drink 2 if you shared your spotify wrapped this year")
                    add("Player X, Player Y and Player Z drink 3 if you skipped leg day this week (or didn't go)")
                    add("Give out as many drinks as nights you've been drinking in a row")
                    add("Drink 1 if you said \"No we can't!\" to Bob the Builder")
                    add("Give out 3 drinks if you have ever been physically escorted out of a venue")
                    add("Introverts take 2 drinks")
                    add("Extroverts give out 2 drinks")
                    add("If you ever used Kik, drink 2 for your emotional scars")
                    add("Player X, drink 1  every tiktok you have been a part of (maximum 10 drinks)")
                },
                55
            ),
            MessageGroup(
                "Action",
                LinkedList<String>().apply {
                    add("Player X, state some advice you want to give to someone in the group, don't ask who - or drink 2") //Action
                    add("Player X, name your biggest turn-off or drink 3") //Action
                    add("Player X, you now control the music, drink 1 if the group hears a bad song") //Action
                    add("FREEZE. Player X throw a coin from where they are, if it lands in someone's cup they must down it") //Action
                    add("FREEZE. Person with the least in their drink, finishes it off")
                    add("Whoever is the least drunk, the group decides how much you drink") //Action
                    add("Player X, choose another player, decide how much you both drink") //Action
                    add("Player X, choose something you have never ever done, if someone has done it they must drink") //Action
                    add("Last person to bring up a photo of another player on their phone must drink 4") //Action
                    add("Player X, choose a rhyme, go round clockwise and the last person to think of a rhyme must drink 3") //Action
                    add("Player X, pour a shot into another player's drink") //Action
                    add("Player X, tell another player a heartfelt compliment, the group will judge if you drink 3 or not") //Action
                    add("Player X, play two truths and a lie, if no one guesses the lie they all drink 2") //Action
                    add("Long arm pint! Player X finish your drink without bending your elbow")
                    add("Everyone play rock paper scissors with the person to your left, overall winner gives out 5 drinks")
                    add("Player X, make a rousing speech to the other players, after which they communally decide how much you drink") //Action
                    add("Player X, imitate Player Y, yes that includes their drinks") //Action
                    add("Take a group selfie to remember the moment, anyone that refuses drinks 3") //Action
                    add("Everyone puts a hand on the table, the last person to take their hand off gives out 5 sips") //Action
                    add("Player X, take a shot like a hero, or give out two shots like a villain") //Action
                    add("Player X, take an item of clothing from each player (e.g., hat), if you refuse drink 3") //Action
                    add("First person to recite the alphabet backwards gives out 5 drinks") //Action
                    add("Everyone puts their phone in the middle, first to get a notification drinks 3") //Action
                    add("Player X, give your phone to the person to the left, they write a text to someone. If you choose not to send it, drink 5") //Action
                    add("Take a drink if you have ever drank in a park, you menace")
                    add("Say 'On My Ship Is' and add an item. Each player must repeat and add a new item. Whoever messes up drinks 1 for each item mentioned")
                    add("Everyone stops speaking! Everyone must drink in alphabetical order. If you mess up then restart")
                    add("Player X, speak about a subject for a minute, you cannot repeat words. Every time you do, take a drink")
                    add("Take a drink for every drug you've ever tried, we're judging you")
                    add("Choose an object in the room, last to touch it drinks 3")
                    add("Player X, tell an embarrassing story or drink 3")
                    add("The first person to come up with an inspirational quote gives out 4 sips")
                    add("Player X must stand on one leg until told otherwise, drink 1 for messing up")
                    add("Player X, make a show of athleticism (e.g., push-ups) or drink 5")
                    add("Player X names a band, Player Y must name 5 of their songs or you both drink 5 instead")
                    add("Player X, choose another player for a staring contest, loser drinks 3")
                    add("Player X, come up with a dare for Player Y, if they refuse they drink 4")
                    add("Player X, choose a song and sing 30 seconds of karaoke or drink 2")
                    add("Stand up! Last player to do so drinks 2")
                    add("STOP TALKING! Player X must state what Player Y last said or drink 2")
                    add("Play hide and seek! Player X seeks, first to be found drinks 2, last to be found gives 2")
                    add("Player X must do a 30-second interpretive dance of whatever Player Y is saying")
                    add("Go round in a circle rhyming with a word Player X chooses, first to mess up drinks 3")
                    add("Player X, sing your national anthem, if it's not passionate enough drink 3")
                    add("Player X hums a tune for 5 seconds, if Player Y can't get it you both drink 4")
                    add("On the count of 3 raise your hand, anyone with the same number of fingers as anyone else drinks 2")
                    add("Player X, explain to the group why you shouldn't drink 5 immediately")
                    add("Player X, assign eye colors to drink 1, 2, or 3 drinks")
                    add("Player X, take 6 sips out of your glass or 1 sip out of your bare palm")
                    add("Player X, please refill your glass without using your hands")
                    add("Player X and Player Y make competing toasts, the worst voted toast drinks 2 and the best gives 2")
                    add("Player X, come out with a catchphrase you must say everytime you drink")
                    add("Player X, choose someone to do 20 push ups")
                    add("Player X, choose someone to do 30 squats")
                    add("Player X, please refill your glass without using your hands")
                    add("All players please bid on the right to give out 6 drinks, bids start at 2 drinks")
                    add("Everyone read out your last social media post - if you refuse drink 2")
                    add("Go round and list bars/clubs you've been to. Those who mess up drink 2")
                    add("Player X, imitate a scene from a movie in silence. First to guess the movie gives out 3 drinks")
                    add("Go round and list bars/clubs you've been to. Those who mess up drink 2")
                    add("Player X, tell a joke. If anyone laughs they drink 2")
                    add("Player X, tell a story and the group decides if you drink 3 or not")
                    add("Player X, please pour a shot into two other players drinks")
                    add("Player X, reveal your truest phobia or take a shot")
                    add("Player X pour Player Y's next drink")
                    add("Every 20 minutes (eg 8:00, 8:20, 8:40) the first player to say \"Pintley\" gives out 5 drinks")
                    add("Couples think back to your last argument. Whoever was in the wrong, drink 5 in shame")
                    add("Whoever removes a piece of clothing first, give out 2 dip drinks")
                    add("Player X, close your eyes and say what everyone is drinking. Give out 1 drink per correct answer, if you mess up drink them")
                    add("Player X, name as many African capitals as you can. Give out 1 drink per answer but if you mess up, drink them")
                    add("Player X, name as many Asian capitals as you can. Give out 1 drink per answer but if you mess up, drink them")
                    add("Player X, name as many South American capitals as you can. Give out 1 drink per answer but if you mess up, drink them")
                    add("Player X, name an accessory. Whoever is wearing that accessory drinks 1")
                    add("Player X drinks for 5 second Player Y does the counting")
                    add("Drink 1 if you're a summer person")
                    add("Drink 1 if you're a winter person")
                    add("If you are wearing a hat, doth it at another player and they must drink 2")
                    add("Left handed people drink 1, ambidexturous people dual wield two drinks and drink them both")
                    add("Everyone says a pickup line, corniest and none-participants drink 1")
                    add("Player X, do an impression of Player Y. The group can give out 3 drinks based on the quality")
                    add("Player X tell us about the time you did or almost shit yourself. Take a shot instead if you're a coward")
                    add("Player X drink the total of your bodycount")
                    add("Player X let another player modify your tindr or take 2 shots. If you don't have one, give out 1 shot")
                    add("Player X must be Player Y's cheerleader everytime they take a drink, motivate them to drink more")
                    add("Player X, tell an offensive joke. If it's not judged offensive enough, drink 4")
                    // Add more action messages here...
                },
                20
            ),
            MessageGroup(
                "Rule",
                LinkedList<String>().apply {
                    add("No eye contact, if anyone happens to make eye contact you both drink 1") //Rule
                    add("If anyone makes eye contact, the first to wink makes the other drink 1") //Rule
                    add("Player X, decide on new names for everyone, if someone messes up they must drink")
                    add("Player X, replace a word of your choice with another word, if anyone uses the old word they drink 1")
                    add("No one can cross their legs or touch their hair. If anyone sees you do it, you drink 1")
                    add("Left Hand Rule! Players must hold their drink with their none dominant hand. Drink 1 if you mess up")
                    add("Each time you refer to a player, everyone must use a different name. Drink 1 if you mess up")
                    add("Player X, Player Y and Player Z are all a team. The team majority chooses how to divide their drinks")
                    add("Everyone is Question-Master! If anyone answers someone elses question then they drink 1")
                    add("All players swap names! Decide who is who and if anyone uses the old name they drink 1")
                    add("Whenever a player drinks they must choose either the person to their left or right to drink with them")
                },
                4
            ),
            MessageGroup(
                "Weakness",
                LinkedList<String>().apply {
                    add("Player X, if you say someone's name, take a drink and they get to write their name on your skin") //Weakness
                    add("Player X must call everyone by their full name, every time you fail drink 1") //Weakness
                    add("Player X, choose a small item to protect, if anyone obtains it then you must drink 4") //Weakness
                    add("Player X is a drink bitch, you must make a drink for anyone that asks you") //Weakness
                    add("Questions only, if Player X makes a statement that isn't a question, they must take a drink") //Weakness
                    add("Player X is feeling a bit slow, if you use any conjunctions (and, but, there, while) take a drink") //Weakness
                    add("Player X, all your drinks are doubled, sorry") //Weakness
                    add("Player X may only use your index fingers to drink, no spilling!")
                    add("Player X, swap drinks with Player Y")
                    add("Player X, you are an aristocrat, raise your pinky whenever you drink or drink again")
                    add("Player X is in court, speak only through whispering to your lawyers sitting to your left and right")
                    add("Player X must do an impression of their choice, each time they mess up they drink 2")
                    add("Player X must end all sentences with \"is that okay!?\" or take a drink")
                    add("Player X, use your least dominant hand to drink, if you mess up drink 1")
                    add("Player X, you must raise your hand to speak, if you mess up drink 1")
                    add("Player X this is a serious matter. Make a serious face, if you break it or laugh, drink 1")
                    add("Player X must end all sentences with \"is that okay!?\" or take a drink")
                    add("Player X, use your least dominant hand to drink, if you mess up drink 1")
                    add("Player X, you must raise your hand to speak, if you mess up drink 1")
                    add("Player X, is down with the kids. Speak only in outdated slang, ya dig")
                    add("Player X, you must say the opposite of what you mean to say")
                    add("Player X, speak only in Ye Olde English, drink 1 if you mess up")
                    add("Player X, you must answer all questions asked truthfully, as if you were Player Y")
                },
                4
            ),
            MessageGroup(
                "Special",
                LinkedList<String>().apply {
                    add("Player X, reflect any drinks you are given, one time")
                    add("Player X, give away a single weakness to another player of your choice")
                    add("Player X, raise your hands in the air. The last person to raise their hands drinks 3")
                    add("Player X, touch your thumb to the table. The last person to also touch their thumb to the table drinks 3")
                    add("Player X, change the player named on another tile")
                    add("Player X, choose to bring a rule back from the graveyard")
                },
                3
            ),
            MessageGroup(
                "Wild",
                LinkedList<String>().apply {
                    add("Player X, decide what this tile says")
                    add("Player X, decide what this tile says")
                    add("Player X, decide what this tile says")
                    add("Player X, choose a democracy question")
                    add("Player X, choose a democracy question")
                    add("Player X, choose a democracy question")
                    add("Player X, decide on a rule")
                    add("Player X, decide on a rule")
                    add("Player X, decide on a rule")
                },
                1
            ),
            MessageGroup(
                "Democracy",
                LinkedList<String>().apply {
                    add("Who is the best dressed?")
                    add("Who is the worst driver?")
                    add("Who is the biggest lightweight?")
                    add("Who is the biggest alcoholic?")
                    add("Who here is the least drunk?")
                    add("Who here is the most fun?")
                    add("Who here is the strongest?")
                    add("Who here will be the most successful in life?")
                    add("Who here is the most privileged?")
                    add("Who here is the biggest social butterfly?")
                    add("Who would you rather be stranded on a deserted island with?")
                    add("Who would you call to hide a body?")
                    add("Who would you rather wake up as?")
                    add("Who would make the best politician?")
                    add("Who is the worst cook?")
                    add("Who is the most likely to ditch on your plans last minute?")
                    add("Who is going to throw up first?")
                    add("Who is going to bed soon?")
                    add("Who is worst at handling spice?")
                    add("Who is the least dominant?")
                    add("Who is the horniest?")
                    add("Who is wearing the most expensive clothes?")
                    add("Who is the most sensitive, oh no?")
                    add("Who has the worst taste in music?")
                    add("Who has the best taste in music?")
                    add("Who was the most popular in school?")
                    add("Who has the highest 'body count'?")
                    add("Who has the weakest drink?")
                    add("Who has the strongest drink?")
                    add("Who is the most athletic?")
                    add("Who is the best gamer?")
                    add("Who has the strangest hobby?")
                    add("Who has the best taste in TV/Movies?")
                    add("Who is the least hygienic?")
                    add("Who is the worst loser?")
                    add("Who is the least employable?")
                    add("Who do you trust the most?")
                    add("Who would you want in a fight?")
                    add("Who is most likely to complain?")
                    add("Who is the biggest bullshitter?")
                    add("Who is the most gullible?")
                    add("Who is the biggest nerd?")
                    add("Who is the best on a night out?")
                    add("Who is the most dramatic?")
                    add("Who is least likely to get into a club?")
                    add("Who is most likely to 'forget' to buy a round?")
                    add("Choose who would win in a fight, Player X or Player Y")
                    add("Who is the loudest?")
                    add("Who is the most confrontational?")
                    add("Who has the hardest job?")
                    add("Who is the most persuasive?")
                    add("Who is the best tipper?")
                    add("Who is most likely to complain at a restaurant?")
                    add("Who is the most adventurous?")
                    add("Who would you least like to live with?")
                    add("Who is most likely to go to sleep first?")
                    add("Who is the worst in bed?")
                    add("Who would make the best warrior?")
                    add("Who would make the best king?")
                    add("Who is the most promiscuous?")
                    add("Who is the life of the party?")
                    add("Who would you want most in a zombie apocalypse?")
                    add("Who would you want as your partner in your fortune 500 company?")
                    add("Who gives the best advice?")
                    add("Who gives the worst advice?")
                    add("Who is the most knowledgeable about pop culture?")
                    add("Who is the most cultured?")
                    add("Who is the best at keeping a secret?")
                    add("Who is the biggest risk taker?")
                    add("Who is the most aggressive?")
                    add("Who will be the first to start a family?")
                    add("Who is the drunkest?")
                    add("Who would perform best in the military?")
                    add("Who is the most practical?")
                    add("Who is the coolest?")
                    add("Who is the groups main organiser?")
                    add("Who is your role model?")
                    add("Who would you want as your negotiator in a hostage situation?")
                    add("Who would you most like to investigate your murder?")
                    add("Whose facts do you trust the most without conformation?")
                    add("Who do you want as your partner in a horror movie?")
                    add("Who is your chosen heist-partner?")
                    add("Who is your chosen prison friend?")
                    add("Who would do the best in prison?")
                    add("Who is the most likely to be guilty of a crime?")
                    add("Who here will people write books about?")
                    add("Who is most likely to join an extremist group?")
                    add("Who is most likely to be successful in staging a revolution?")
                    add("Who should really speak less?")
                    add("Who should really speak more?")
                    add("Who do you trust most to speak on your behalf?")
                    add("Who do you trust least to speak on your behalf?")
                    add("Who is the happiest in the room right now?")
                    add("Who is the best storyteller?")
                    add("Who is the most passionate?")
                    add("Who do you trust least to plan a holiday?")
                    add("Who is always causing drama?")
                    add("Who is the most resilient?")
                    add("Who is the most optimistic?")
                    add("Who is the meanest?")
                    add("Who would make the best supervillain?")
                    add("Who has the best relationship with their parent(s)?")
                    add("Who is the shotcaller?")
                    add("Who is the worst swimmer?")
                    add("Who is the parent of the group?")
                    add("Who would go the furthest to help you?")
                    add("Who is the most elitist?")
                },
                15
            ),
            MessageGroup(
                "Power",
                LinkedList<String>().apply {
                    add("Player X, choose a friend, whenever you drink they drink")
                    add("Player X, you are the group's boss. Inappropriate workplace conduct must be punished with drinks.")
                    add("Player X, others must ask your permission before having a drink, if they forget they drink double")
                    add("Player X is a concerned father, all players must refer to you as daddy. Mess up and drink 1") //Power
                    add("Player X is Question-Master, anyone who answers any of your questions must drink 1")
                    add("Player X is Passion-Master, you can assign 1 drink to anyone not showing enough passion for the night")
                    add("Player X, whenever you make two finger guns, everyone must freeze. The last to do so drinks 2") //Power
                    add("Player X is a symbol monkey, everytime they clap their hands everyone drinks 1 including themselves")
                    add("Player X is question-master, anyone who answers any of your questions must drink 1")
                    add("Player X, whenever you make a person laugh they must now drink 1")
                    add("Player X is immune to the effects of rules")
                    add("Player X is immune to the next 5 tiles, Player Y must drink them instead")
                },
                4
            ),
            MessageGroup(
                "Elimination",
                LinkedList<String>().apply {
                    add("All Rules are out of play")
                    add("All Powers are out of play")
                    add("All Weaknesses are out of play")
                    add("All Rules/Powers/Weaknesses are out of play")
                    add("All Rules are out of play")
                    add("All Powers are out of play")
                    add("All Weaknesses are out of play")
                    add("All Rules/Powers/Weaknesses are out of play")
                    add("All Rules are out of play")
                    add("All Powers are out of play")
                    add("All Weaknesses are out of play")
                    add("All Rules/Powers/Weaknesses are out of play")
                },
                2
            ),
            MessageGroup(
                "Would You Rather",
                LinkedList<String>().apply {
                    add("Would you rather live agelessly for 100 years or receive a billion dollars?")
                    add("Would you rather the ability to breath underwater or the ability to fall from any height?")
                    add("Would you rather receive a life sentence or the death penalty?")
                    add("Would you rather only eat sweet food or only eat spicy food?")
                    add("Would you rather live without social media or without streaming services?")
                    add("Would you rather have permanent free travel or a super car?")
                    add("Would you rather be the richest person in the world and hated or broke but loved?")
                    add("Would you rather have street smarts or book smarts?")
                    add("Would you rather have permanent summer or permanent winter?")
                    add("Would you rather be reincarnated with your past memories and no skill or skills but no memory?")
                    add("Would you rather have immortality or reincarnation with intact memory?")
                    add("Would you rather eat the same meal forever or only be able to drink water?")
                    add("Would you rather be able to lift a car but look super weak or have the body of a Greek God but be moderate in strength?")
                    add("Would you rather have a million dollars or a 50% chance of 20 million dollars?")
                    add("Would you rather lose your hearing or lose your eyesight?")
                    add("Would you rather save 10 random people but sacrifice yourself or sacrifice 10 and save yourself?")
                    add("Would you rather have an amazing house or amazing holidays?")
                    add("Would you rather lie for a cheating friend or snitch on a cheating friend?")
                    add("Would you rather be the strongest man in the world by far or be the best at chess in the world by far?")
                    add("Would you rather be a perfect artist or a perfect mathematician?")
                    add("Would you rather have only one pet or only one child?")
                    add("Would you rather have the hottest partner or the smartest partner?")
                    add("Would you rather go into witness protection and never see your family or have 100 kids right now?")
                    add("Would you rather be able to control water or be able to control fire?")
                    add("Would you rather be permanently bald or have werewolf hair growth?")
                    add("Would you rather only watch movies or only listen to movies?")
                    add("Would you rather always be an hour late or 2 hours early?")
                    add("Would you rather read people's thoughts or read people's memories?")
                    add("Would you rather be unable to listen to new music or be unable to relisten to music?")
                    add("Would you rather find true love but never find work or have a successful career without ever finding love?")
                    add("Would you rather live in the Sahara Desert or Antarctica?")
                    add("Would you rather be able to code like a genius or play an instrument like a genius?")
                    add("Would you rather be ghosted by your crush or told you aren't liked by your crush?")
                    add("Would you rather be unable to shower or unable to brush your teeth?")
                    add("Would you rather know how you will die or when you will die?")
                    add("Would you rather have a personal chef or have a personal driver?")
                    add("Would you rather have two extra arms or have a third eye?")
                    add("Would you rather never break anything or never lose anything?")
                    add("Would you rather be smart but everyone depends on you or be stupid but completely taken care of?")
                    add("Would you rather choose where you go for dinner or have someone else choose?")
                    add("Would you rather have a great memory be a comedic genius?")
                    add("Would you rather date someone similar to you or different to you?")
                    add("Would you rather live in a world without war or live in a world without poverty?")
                    add("Would you rather live underwater or in space?")
                    add("Would you rather never have to sleep or never get sick?")
                    add("Would you rather be able to talk to animals all speak all languages fluently?")
                    add("Would you rather move objects with your mind or have super speed?")
                    add("Would you rather be immune to alcohol or be immune to recreational drugs?")
                    add("Would you rather have everyone respect you or desire you?")
                    add("Would you rather be a dog or a cat?")
                    add("Would you rather have a photographic memory or forget anything at will?")
                    add("Would you rather win the lottery or find true love?")
                    add("Would you rather be a lightweight or a heavyweight drinker?")
                    add("Would you rather have a world ruled by passion or logic?")
                    add("Would you rather own a 5 star hotel or a 3 michelin star restaurant?")
                    add("Would you rather swim 10 meters in 50°C or crawl 10 meters across burning coals?")
                    add("Would you rather be the best fighter in the world or the quickest reader?")
                    add("Would you rather sleep with Player X or Player Y?")
                    add("Would you rather earn double the money or work half as much for the same pay?")
                    add("Would you rather live in the current day as a regular person or 100 years ago as royalty?")
                    add("Would you rather be known as a prostitute or a fraudster who targets the elderly?")
                    add("Would you rather start your business empire with Player X or Player Y?")
                    add("Would you rather be Prime Minister or their most trusted advisor?")
                    add("Would you rather travel 40 years into the past or future?")
                    add("Would you rather have sex in the shower or the back of a car?")
                    add("Would you rather kill one person to save three, or not kill anyone and let three die?")
                    add("Would you rather not cheat on your partner but have them think you did or cheat on them but they think you didn't?")
                    add("Would you rather be addicted to cocaine or gambling?")
                    add("Would you rather fight like Mike Tyson or Bruce Lee?")
                    add("Would you rather serve in Heaven or rule in Hell")
                },
                6
            ),
        )
        messageGroups = preparedMessageGroups.map { group ->
            val preparedMessages = group.messages.map { message ->
                val placeholders = listOf("Player X", "Player Y", "Player Z")
                val shuffledPlayerNames = playerNames.shuffled()
                placeholders.foldIndexed(message) { index, acc, placeholder ->
                    if (acc.contains(placeholder) && shuffledPlayerNames.size > index) {
                        acc.replace(placeholder, shuffledPlayerNames[index])
                    } else {
                        acc
                    }
                }
            }
            MessageGroup(group.name, LinkedList(preparedMessages), group.weight)
        }

        val messageText: TextView = findViewById(R.id.message_text)
        val layout: FrameLayout = findViewById(R.id.BackgroundLayout)
        val nextarea: View = findViewById(R.id.next_area)
        val backarea: View = findViewById(R.id.back_area)
        tileTypeTextView = findViewById(R.id.tile_type_text)
        val tileOverlay: View = findViewById(R.id.tile_overlay) // Add this line

        nextarea.visibility = View.VISIBLE
        backarea.visibility = View.VISIBLE

        tileOverlay.bringToFront() // Bring the tile_overlay view to the front

        // Initialize a stack to keep track of previous messages
        val messageStack: Stack<String> = Stack()

        tileOverlay.setOnClickListener {
            val currentMessageGroup = messageGroups.firstOrNull { it.name == tileTypeTextView.text.toString() }
            val specialMessage = currentMessageGroup?.let { getSpecialMessage(it.name) }
            specialMessage?.let { showMessage(it) }
            Log.d("Button Click", "Tile Overlay Button Clicked")
        }


        nextarea.setOnClickListener {
            if (messageGroups.isNotEmpty()) {
                val randomGroup = weightedRandom(messageGroups)
                if (randomGroup.messages.isNotEmpty()) {
                    val randomIndex = random.nextInt(randomGroup.messages.size)
                    val randomMessage = randomGroup.messages.removeAt(randomIndex)

                    // Push the current message onto the stack
                    messageStack.push(randomMessage)

                    // Update the message text
                    messageText.text = randomMessage

                    // Update the tile type
                    tileTypeTextView.text = randomGroup.name

                    // Change the background color depending on the message group
                    val colorResource = when (randomGroup.name) {
                        "Regular" -> R.color.LightPurple
                        "Action" -> R.color.LightRed
                        "Rule" -> R.color.Gold
                        "Weakness" -> R.color.SicklyGreen
                        "Special" -> R.color.Violet
                        "Wild" -> R.color.ForestGreen
                        "Democracy" -> R.color.LightBlue
                        "Power" -> R.color.CrimsonRed
                        "Elimination" -> R.color.Granite
                        "Would You Rather" -> R.color.Flamingo
                        else -> throw IllegalArgumentException("Unknown group name")
                    }
                    layout.setBackgroundColor(ContextCompat.getColor(this, colorResource))
                }

                if (randomGroup.messages.isEmpty()) {
                    messageGroups = messageGroups.filterNot { it == randomGroup }
                }

                if (messageGroups.isEmpty()) {
                    messageText.text = "You completed Pintley?"
                }

                Log.d("Button Click", "Next Area Button Clicked")
            }
        }

        backarea.setOnClickListener {
            if (messageStack.size > 1) {
                // Pop the current message from the stack
                messageStack.pop()

                // Retrieve the previous message without removing it
                val previousMessage = messageStack.peek()

                // Set the previous message as the current message
                messageText.text = previousMessage

                Log.d("Button Click", "Back Area Button Clicked")
            }
        }
    }
        private fun weightedRandom(items: List<MessageGroup>): MessageGroup {
        val totalWeight = items.sumOf { it.weight }
        var randomIndex = random.nextInt(totalWeight)
        for (item in items) {
            randomIndex -= item.weight
            if (randomIndex < 0) {
                return item
            }
        }

        throw IllegalStateException("The weights in the list do not sum up correctly.")
    }

    private fun getSpecialMessage(messageClass: String): String? {
        return when (messageClass) {
            "Regular" -> "If no one is named, ask everyone the question"
            "Action" -> "This is a task for whoever is named"
            "Power" -> "This is a benefit that a single player holds until stated otherwise"
            "Weakness" -> "This is a penalty that a single player holds until stated otherwise"
            "Rule" -> "Rules effect everyone and stay in place until stated otherwise"
            "Wild" -> "These are up to you, have some fun"
            "Democracy" -> "Everyone close your eyes and point to who you think the answer is, players in the minority drink"
            "Special" -> "A player may use this once, whenever they want"
            "Elimination" -> "Remove something from play. We recommend doing this freely rather than using the prompts"
            "Would You Rather" -> "Everyone close your eyes. Thumbs up for Option 1, thumbs down for Option 2. The minority drinks"
            // Add other special messages for different message groups here...
            else -> null
        }
    }
}
