import java.awt.*;
import acm.program.*;
import acm.graphics.*;
import acm.util.RandomGenerator;
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.text.*;

public class Hangman extends GraphicsProgram
{
  public String[] dictionary;
  
  //Logic Controlling Vars
  public boolean gameState = false;  
  public boolean gameRunBefore = false;
  public String selectedWord = "";
  public String fixedSelectedWord = "";
  public char[] incorrectGuesses;
  public char[] correctGuesses;
  public char[] wordArray;
  int wrongGuesses = 0;
  int rightGuesses = 0;
  
  //GUI
  JTextField guess;
  JButton submit;
  JButton start;
  JButton stop;
  
  //Hangman
  GLine base;
  GLine column;
  GLine overhang;
  GLine noose;
  GOval head;
  GLine body;
  GLine lArm;
  GLine rArm;
  GLine lLeg;
  GLine rLeg;
  GLabel[][] wrongLetters;
  GLabel bankLabel;
  GLabel answerLine;
  GLabel[] answerLetters;
  GLabel statusLabel;
  
  //Constants
  public static final int APPLICATION_WIDTH = 480;
  public static final int APPLICATION_HEIGHT = 320;
  public static final int LABEL_OFFSET = 15;
  public static final int BASE_WIDTH = 50;
  public static final int COLUMN_HEIGHT = 100;
  public static final int OVERHANG = 0;
  public static final int OVERHANG_WIDTH = 50;
  public static final int NOOSE_DEPTH = 10;
  public static final int HEAD_DIAMETER = 15;
  public static final int BODY_DEPTH = 40;
  public static final int LIMB_WIDTH = 15;
  public static final int LIMB_HEIGHT = 15;
  public static final int WRONG_LETTERS_ROWS = 6;
  public static final int WRONG_LETTERS_COLUMNS = 5;
  
  //Constants calculated from other constants
  public static final int CENTER_X = APPLICATION_WIDTH / 2;
  public static final int CENTER_Y = APPLICATION_HEIGHT / 2;
  public static final int BASE_X1 = CENTER_X - (BASE_WIDTH / 2);
  public static final int COLUMN_Y2 = CENTER_Y - COLUMN_HEIGHT;
  public static final int OVERHANG_X2 = CENTER_X + OVERHANG_WIDTH;
  public static final int NOOSE_Y2 = COLUMN_Y2 + NOOSE_DEPTH;
  public static final int BODY_Y1 = NOOSE_Y2 + HEAD_DIAMETER;
  public static final int BODY_Y2 = BODY_Y1 + BODY_DEPTH;
  public static final int ARM_Y1 = BODY_Y1 + (BODY_DEPTH / 2);
  public static final int ARM_Y2 = ARM_Y1 - LIMB_HEIGHT;
  public static final int LEG_Y2 = BODY_Y2 + LIMB_HEIGHT;
  
  public void init()
  {
    //Load the dictionary
    int count = 0;
    File file = new File("dictionary.txt");
    if (file.exists() == true)
    {
      try
      {
        Scanner fin = new Scanner(file);       
        while(fin.hasNextLine())
        {
          count++;
          fin.nextLine();
        }
        fin.close();
      }
      catch (FileNotFoundException ex)
      {
        System.err.println(ex.toString());
      }
    }    
    dictionary = new String[count];
    if (file.exists() == true)
    {
      try
      {
        Scanner fin = new Scanner(file);       
        for (int i = 0; i < count; i++)
        {
          dictionary[i] = fin.nextLine();
        }
        fin.close();
      }
      catch (FileNotFoundException ex)
      {
        System.err.println(ex.toString());
      }
    }   
    
    //Construct GUI
    guess = new JTextField(1);
    submit = new JButton("Guess!");
    start = new JButton("Start Game");
    stop = new JButton("Stop Game");
    statusLabel = new GLabel("Waiting to Start Game");
    
    //Construct Hangman
    base = new GLine(BASE_X1, CENTER_Y, CENTER_X + (BASE_WIDTH / 2), CENTER_Y);
    column = new GLine(CENTER_X, CENTER_Y, CENTER_X, COLUMN_Y2);
    overhang = new GLine(CENTER_X - OVERHANG, COLUMN_Y2, OVERHANG_X2, COLUMN_Y2);
    noose = new GLine(OVERHANG_X2, COLUMN_Y2, OVERHANG_X2, NOOSE_Y2);
    head = new GOval(OVERHANG_X2 - (HEAD_DIAMETER / 2), NOOSE_Y2, HEAD_DIAMETER, HEAD_DIAMETER);
    body = new GLine(OVERHANG_X2, BODY_Y1, OVERHANG_X2, BODY_Y2); 
    lArm = new GLine(OVERHANG_X2, ARM_Y1, OVERHANG_X2 - LIMB_WIDTH, ARM_Y2);
    rArm = new GLine(OVERHANG_X2, ARM_Y1, OVERHANG_X2 + LIMB_WIDTH, ARM_Y2);
    lLeg = new GLine(OVERHANG_X2, BODY_Y2, OVERHANG_X2 - LIMB_WIDTH, LEG_Y2);
    rLeg = new GLine(OVERHANG_X2, BODY_Y2, OVERHANG_X2 + LIMB_WIDTH, LEG_Y2);
    
    //Create space for incorrect guesses
    wrongLetters = new GLabel[WRONG_LETTERS_COLUMNS][WRONG_LETTERS_ROWS];
    for (int i = 0; i < WRONG_LETTERS_COLUMNS; i++)
    {
      for (int n = 0; n < WRONG_LETTERS_ROWS; n++)
      {
        wrongLetters[i][n] = new GLabel(" ");
        add(wrongLetters[i][n], 100 + (i * LABEL_OFFSET), 100 + (n * LABEL_OFFSET));
      }
    }
    bankLabel = new GLabel("Wrong Guesses:");
    add(bankLabel, 100, 100 - LABEL_OFFSET);
    
    //Add GUI
    add(guess, SOUTH);
    add(submit, SOUTH);
    add(start, NORTH);
    add(statusLabel, CENTER_X - (statusLabel.getWidth() / 2), 10);
    
    //Add Hangman
    add(base);
    add(column);
    add(overhang);
    add(noose);
    add(head);
    add(body);
    add(lArm);
    add(rArm);
    add(lLeg);
    add(rLeg);
    
    addActionListeners();
  }
  
  public void actionPerformed(ActionEvent e)
  {
    //See what button was pressed
    if (e.getSource() == start && gameState == false)
    {
      //if the game isn't started
      //Start the game
      //Select a word
      RandomGenerator myRandom = new RandomGenerator();
      int index = myRandom.nextInt(0, dictionary.length - 1);
      selectedWord = dictionary[index];
      fixedSelectedWord = selectedWord.toLowerCase();
      System.out.println(fixedSelectedWord);
      
      
      //Reset all vars
      wrongGuesses = 0;
      rightGuesses = 0;
      incorrectGuesses = new char[26];
      correctGuesses = new char[26];
      
      //Remove Hangman
      remove(base);
      remove(column);
      remove(overhang);
      remove(noose);
      remove(head);
      remove(body);
      remove(lArm);
      remove(rArm);
      remove(lLeg);
      remove(rLeg);
      
      //Removing the answer spots if the game has been run before to make room for new ones
      if (gameRunBefore == true)
      {
        for (int i = 0; i < answerLetters.length; i++)
        {
          remove(answerLetters[i]);
          remove(answerLine);
        }
      }
      
      //Create new GUI
      //add(stop, NORTH);
      answerLine = new GLabel("");
      //addActionListeners();
      start.setText("Stop Game");
      
      //Populate the answer line with _s if the char in that spot isn't a space
      for (int i = 0; i < selectedWord.length() - 1; i++)
      {
        if (!(selectedWord.charAt(i) == ' '))
        {
          answerLine.setLabel(answerLine.getLabel() + "_ ");
        }
        else
        {
          answerLine.setLabel(answerLine.getLabel() + "   ");
        }
      }
      if (!(selectedWord.charAt(selectedWord.length() - 1) == ' '))
      {
        answerLine.setLabel(answerLine.getLabel() + "_");
      }
      else
      {
        answerLine.setLabel(answerLine.getLabel() + "  ");
      }
      
      //A label for offset purposes, don't add to gui
      GLabel widthLabel = new GLabel("_ ");
      
      //Determining the number of answer spots needed
      answerLetters = new GLabel[selectedWord.length()];
      
      //Some positioning
      double answerLineX = (APPLICATION_WIDTH / 2) - (answerLine.getWidth() / 2);
      double answerLineY = (APPLICATION_HEIGHT / 2) + 50;
      
      wordArray = new char[selectedWord.length()];
      correctGuesses = new char[selectedWord.length()];
      
      //Populate the answer spots with spaces and offseting them
      for (int i = 0; i < selectedWord.length(); i++)
      {
        answerLetters[i] = new GLabel(" ");
        answerLetters[i].setLocation(answerLineX + (i * widthLabel.getWidth()), answerLineY - 2);
        add(answerLetters[i]);
        wordArray[i] = selectedWord.charAt(i);
      }
      
      //Clear out incorrect answer spots
      for (int i = 0; i < WRONG_LETTERS_COLUMNS; i++)
      {
        for (int n = 0; n < WRONG_LETTERS_ROWS; n++)
        {
          wrongLetters[i][n].setLabel(" ");
        }
      }
      
      //"guess" a space for words that have spaces since players don't have to guess spaces
      guess(' ', false, true);
      add(answerLine, answerLineX, answerLineY);
      System.out.println(selectedWord);
      gameState = true;
      gameRunBefore = true;
      updateStatus("Game Started");
    }
    else if (e.getSource() == start && gameState == true)
    {    
      stopGame(true, false);
    }
    if (e.getSource() == submit && gameState == true && guess.getText().length() == 1)
    {
      guess(guess.getText().charAt(0), true, false);
    }
  }  
  
  public void stopGame(boolean playerStopped, boolean playerWon)
  {
    start.setText("Start Game");
    gameState = false;
    System.out.println("stopped");
    if (playerStopped == true)
    {
      add(base);
      add(column);
      add(overhang);
      add(noose);
      add(head);
      add(body);
      add(lArm);
      add(rArm);
      add(lLeg);
      add(rLeg);    
      updateStatus("Game Stopped");
    }
    else
    {
      updateStatus("Game Over!");
    }
    // for (int i = 0; i < answerLetters.length; i++)
    // {
    //   remove(answerLetters[i]);
    // }
    for (int i = 0; i < answerLetters.length; i++)
    {
      answerLetters[i].setLabel("" + wordArray[i]);
    }
    //remove(answerLine);
    if (playerWon == true)
    {
      updateStatus("You Won!");
    }
  }
  
  public void updateStatus(String newLabel)
  {
    statusLabel.setLabel(newLabel);
    statusLabel.setLocation(CENTER_X - (statusLabel.getWidth() / 2), 10);
  }
  
  public void guess(char theGuess, boolean mattersIfLetter, boolean overrideGuessPenalty)
  {
    if (Character.isLetter(theGuess) == true || mattersIfLetter == false) //Make sure the guess is a letter
    {
      System.out.println("good");
      //Check to make sure 1) the guess is correct and 2) it hasn't been guessed already
      boolean isNotAlreadyCorrect = true;
      boolean isCorrectGuess = false;
      int[] positions = new int[wordArray.length];
      int n = 0;
      for (int i = 0; i < wordArray.length; i++)
      {
        if (theGuess == wordArray[i])
        {
          isCorrectGuess = true;
          positions[n] = i;
          n++;
        }
        if (theGuess == correctGuesses[i])
        {
          isNotAlreadyCorrect = false;
        }
      }
      if (isCorrectGuess == true && isNotAlreadyCorrect == true) //it is correct and hasn't been guessed
      {
        System.out.println("correct");
        for (int i = 0; i < correctGuesses.length; i++)
        {
          //Add the guess to the next open spot in correctGuesses
          System.out.println(correctGuesses[i]);
          if (correctGuesses[i] == '\u0000')
          {
            correctGuesses[i] = theGuess;
            System.out.println("done");
            break;
          }
        }
        for (int i = 0; i < positions.length; i++)
        {
          System.out.println("setting");
          if (!(i == 0) && positions[i + 1] == 0 && positions[i] == 0)
            //duplicate 0 means it's not actually a position
          {
            System.out.println("break");
            break;
          }
          else
          {
            //Place the guess in every instance of that letter
            answerLetters[positions[i]].setLabel("" + theGuess);
            System.out.println("set");
          }
        }
        rightGuesses++;
        if (rightGuesses == wordArray.length)
        {
          stopGame(false, true);
        }
      }
      else if (isCorrectGuess == false)
      {
        System.out.println("wrong");
        boolean isAlreadyIncorrect = false;
        //Check if it has been guessed before
        for (int i = 0; i < incorrectGuesses.length; i++)
        {
          if (theGuess == incorrectGuesses[i])
          {
            isAlreadyIncorrect = true;
          }
        }
        if (isAlreadyIncorrect == false)
        {
          //Populate the next available spot in incorrectGuesses
          for (int i = 0; i < incorrectGuesses.length; i++)
          {
            System.out.println(incorrectGuesses[i]);
            if (incorrectGuesses[i] == '\u0000')
            {
              incorrectGuesses[i] = theGuess;
              System.out.println("done");
              break;
            }
          }
          //Increase the count of wrong guesses if overrideGuessPenalty isn't true
          if (overrideGuessPenalty == false)
          {
            wrongGuesses++;
          }
          //Draw the hangman & check if the game is over
          switch (wrongGuesses)
          {
            case 1:
              add(base);
              wrongLetters[0][0].setLabel("" + theGuess);
              break;
            case 2:
              add(column);
              wrongLetters[1][0].setLabel("" + theGuess);
              break;
            case 3:
              add(overhang);
              wrongLetters[2][0].setLabel("" + theGuess);
              break;
            case 4:
              add(noose);
              wrongLetters[3][0].setLabel("" + theGuess);
              break;
            case 5:
              add(head);
              wrongLetters[4][0].setLabel("" + theGuess);
              break;
            case 6:
              add(body);
              wrongLetters[0][1].setLabel("" + theGuess);
              break;
            case 7:
              add(lArm);
              wrongLetters[1][1].setLabel("" + theGuess);
              break;
            case 8:
              add(rArm);
              wrongLetters[2][1].setLabel("" + theGuess);
              break;
            case 9:
              add(lLeg);
              wrongLetters[3][1].setLabel("" + theGuess);
              break;
            case 10:
              add(rLeg);
              wrongLetters[4][1].setLabel("" + theGuess);
              stopGame(false, false);
              break;
          }
        }
      }
    }
  }
}