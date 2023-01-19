package com.example.texttospeechtest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var sayButton: Button
    private lateinit var translateButton: Button
    private lateinit var songTextMultiline : EditText
    private lateinit var tts: TextToSpeech
    private lateinit var translationResult: TextView

    private var translators: MutableList<Translator> = ArrayList<Translator>()
    private var translatorsRestore: MutableList<Translator> = ArrayList<Translator>()
    private  lateinit var currentTranslator: Translator
    private var count: Int = 0

    private var translatedText = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sayButton = findViewById(R.id.sayButton)
        sayButton.isEnabled = false

        songTextMultiline = findViewById(R.id.songTextMultiline)

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                sayButton.isEnabled = true
                tts.language = Locale.forLanguageTag("ru")
            }
        }

        sayButton.setOnClickListener {
            val text = songTextMultiline.text.toString()

            if(translatedText.isNotEmpty()) {
                tts.speak(translatedText, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }

        translationResult = findViewById(R.id.translationResult)



        translateButton = findViewById(R.id.translateButton)
        translateButton.isEnabled = false
        translateButton.setOnClickListener {
            translatedText = songTextMultiline.text.toString()
            translate {
                Toast.makeText(this, "SUCCESS TRANSLATE ALL", Toast.LENGTH_SHORT).show()
                translationResult.text = translatedText
            }
        }

        val enableTranslateButtonFn = {
            count++
            if(count == translators.size) {
                translateButton.isEnabled = true
            }
        }

//        this.translators.add(makeTranslator(TranslateLanguage.RUSSIAN, TranslateLanguage.SPANISH, enableTranslateButtonFn))
//        this.translators.add(makeTranslator(TranslateLanguage.SPANISH, TranslateLanguage.ENGLISH, enableTranslateButtonFn))
//        this.translators.add(makeTranslator(TranslateLanguage.ENGLISH, TranslateLanguage.RUSSIAN, enableTranslateButtonFn))

//        this.translators.add(makeTranslator(TranslateLanguage.RUSSIAN, TranslateLanguage.ENGLISH, enableTranslateButtonFn))
//        this.translators.add(makeTranslator(TranslateLanguage.ENGLISH, TranslateLanguage.SPANISH, enableTranslateButtonFn))
//        this.translators.add(makeTranslator(TranslateLanguage.SPANISH, TranslateLanguage.RUSSIAN, enableTranslateButtonFn)) this better

        this.translators.add(makeTranslator(TranslateLanguage.RUSSIAN, TranslateLanguage.CHINESE, enableTranslateButtonFn))
        this.translators.add(makeTranslator(TranslateLanguage.CHINESE, TranslateLanguage.JAPANESE, enableTranslateButtonFn))
        this.translators.add(makeTranslator(TranslateLanguage.JAPANESE, TranslateLanguage.CHINESE, enableTranslateButtonFn))
        this.translators.add(makeTranslator(TranslateLanguage.CHINESE, TranslateLanguage.RUSSIAN, enableTranslateButtonFn))



    }

    private fun makeTranslator(fromLocale: String, toLocale: String, successCallback: () -> Unit): Translator {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(fromLocale)
            .setTargetLanguage(toLocale)
            .build()


        val translator = Translation.getClient(options)

        var conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                successCallback()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Can't load ML for translate", Toast.LENGTH_SHORT).show()
            }

        return translator
    }

    private fun translate(successCallback: () -> Unit) {
        if(translators.size != 0) {
            currentTranslator = translators.removeAt(0)
            currentTranslator.translate(translatedText)
                .addOnSuccessListener { result ->
                    run {
                        translatedText = result
                        println("RES: $result")
                        println("TRANSLATED TEXT: $translatedText")
                        translate(successCallback)
                    }
                }
                .addOnFailureListener {
                    println("can't translate(((")
                }

            translatorsRestore.add(currentTranslator)
        } else {
            successCallback()
            translators =  translatorsRestore
            translatorsRestore = ArrayList<Translator>()
        }
    }
}