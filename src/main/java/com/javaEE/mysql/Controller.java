package com.javaEE.mysql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by p1614402 on 24/05/2017.
 */

@RestController
public class Controller {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    UtilisateurRepository utilisateurRepo;

    @Autowired
    TweetRepository tweetRepo;

    @RequestMapping("/count")
    public int countTweets() {
        return jdbcTemplate.queryForObject("select count(*) from tweets", Integer.class);
    }

    @RequestMapping("/list")
    public List<String> getTweets(@RequestParam(value = "auteur", required = false) String auteur) {
        if(auteur != null) {

            return this.jdbcTemplate.query(
                    "select r.tweet, t.date, t.contenu, t.auteur, count(r.utilisateur) from retweets r " +
                            "join tweets t on r.tweet = t.id where auteur =? group by tweet",
                    (rs, rowNum) -> {
                        Tweet tweet = new Tweet();
                        tweet.setId(rs.getInt("r.tweet"));
                        tweet.setDate(rs.getTimestamp("t.date"));
                        tweet.setContenu(rs.getString("t.contenu"));
                        tweet.setAuteur(rs.getString("t.auteur"));

                        String result = " Tweet n° " + tweet.getId() +
                                " : " + rs.getInt("count(r.utilisateur)") + " retweet(s)" ;
                        return result;
                    }, auteur);
        }

        return this.jdbcTemplate.query(
                "select r.tweet, t.date, t.contenu, t.auteur, count(r.utilisateur) from retweets r " +
                        "join tweets t on r.tweet = t.id group by tweet;",
                (rs, rowNum) -> {
                    Tweet tweet = new Tweet();
                    tweet.setId(rs.getInt("r.tweet"));
                    tweet.setDate(rs.getTimestamp("t.date"));
                    tweet.setContenu(rs.getString("t.contenu"));
                    tweet.setAuteur(rs.getString("t.auteur"));

                    String result = " Tweet n° " + tweet.getId() +
                            " : " + rs.getInt("count(r.utilisateur)") + " retweet(s)" ;
                    return result;
                });
    }

    @RequestMapping(value="/tweet", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public void createTweet(@RequestParam String auteur, @RequestParam String contenu) {

        Tweet tweet = new Tweet();
        tweet.setAuteur(auteur);
        tweet.setContenu(contenu);

        tweetRepo.save(tweet);
    }

    @RequestMapping(value="/tweet/{id}")
    @ResponseBody
    public Tweet getTweet(@PathVariable("id") String id) {
        return this.tweetRepo.findOne(Integer.parseInt(id));
    }

    @RequestMapping("/utilisateurs")
    public Iterable<Utilisateur> getUtilisateurs() {
        return this.utilisateurRepo.findAll();
    }

    @RequestMapping(value="/retweet", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public ResponseEntity<HttpStatus> createRetweet(@RequestParam String utilisateurHandle, @RequestParam String tweetId) {

        Utilisateur utilisateurRetweet = utilisateurRepo.findOne(utilisateurHandle);
        Tweet retweet = tweetRepo.findOne(Integer.parseInt(tweetId));
        String auteurTweet = retweet.getAuteur();

        if(!utilisateurHandle.equals(auteurTweet)) {
            retweet.getRetweeters().add(utilisateurRetweet);
            tweetRepo.save(retweet);
        }
        else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}
