package com.mygdx.game.fb_teste;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import java.util.Random;

public class Jogo extends ApplicationAdapter
{
	//Variaveis
	private int pontos = 0;
	private int estadoJogo = 0;
	private int pontuacaoMaxima = 0;
	private float posicaoInicialVertical = 0;
	private float posicaoHorizontalPassaro = 0;
	private float espacoCano;
	private float gravidade = 0;
	private float variacao = 0;
	private float posicaoCanoHorizontal;
	private float posicaoCanoVertical;
	private boolean passouCano = false;
	private Random random;

    //Sprites
    private SpriteBatch batch;
    private Texture[] passaros;
    private Texture fundo;
    private Texture canoBaixo;
    private Texture canoCima;
    private Texture gameOver;


    //Define o Tamanho do Dispositivo
    private float larguraDispositivo;
    private float alturaDispositivo;

	private ShapeRenderer shapeRenderer;
	private Circle circuloPassaro;
	private Rectangle retanguloCanocima;
	private Rectangle retanguloCanobaixo;

	BitmapFont textoPontuacao;
	BitmapFont textoIniciar;
	BitmapFont textoMelhorPontuacao;

	Sound somVoando;
    Sound somColisao;
    Sound somPontuacao;

    Preferences preferencias;

	@Override
	public void create ()
	{
		inicializaTexturas();
		inicializaObjetos();
	}

	@Override
	public void render ()
	{
		verificaEstadojogo();
		desenharTexturas();
		detectarColisao();
		validaPontos();
	}

	private void inicializaTexturas() {
		//Atribui as texturas
		fundo = new Texture("fundo.png");
		passaros = new Texture[3];
		passaros[0] = new Texture("passaro1.png");
		passaros[1] = new Texture("passaro2.png");
		passaros[2] = new Texture("passaro3.png");
		canoCima = new Texture("cano_topo_maior.png");
		canoBaixo = new Texture("cano_baixo_maior.png");
		gameOver = new Texture("game_over.png");
	}

	private void inicializaObjetos() {
		random = new Random();
		batch = new SpriteBatch();
		//Espaço do cano
		espacoCano = 350;
		//Tamanho do dispositivo
		larguraDispositivo = Gdx.graphics.getWidth();
		alturaDispositivo = Gdx.graphics.getHeight();
		//Setta a posição do jogador em relação ao tamanho do dispositivo
		posicaoInicialVertical = alturaDispositivo / 2;
		posicaoCanoHorizontal = larguraDispositivo;

		//Coloca texto na tela do game
		textoPontuacao = new BitmapFont();
		textoPontuacao.setColor(Color.WHITE);
		textoPontuacao.getData().setScale(10);

		textoIniciar = new BitmapFont();
		textoIniciar.setColor(Color.GREEN);
		textoIniciar.getData().setScale(3);

		textoMelhorPontuacao = new BitmapFont();
		textoMelhorPontuacao.setColor(Color.RED);
		textoMelhorPontuacao.getData().setScale(3);

		shapeRenderer = new ShapeRenderer();
		circuloPassaro = new Circle();
		retanguloCanobaixo = new Rectangle();
		retanguloCanocima = new Rectangle();

		somVoando = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
		somColisao = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
		somPontuacao = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));

		preferencias = Gdx.app.getPreferences("flappyBird");
		pontuacaoMaxima = preferencias.getInteger("pontuacaoNaximma", 0);

	}



	// valida seus pontos quando passa pelos canos
	private void validaPontos() {
		if (posicaoCanoHorizontal<50-passaros[0].getWidth())
		{
			if(!passouCano)
			{
				pontos++;
				passouCano = true;
				somPontuacao.play();
			}
		}
		variacao += Gdx.graphics.getDeltaTime() * 10;
		//verifica se é maior que tres
		if(variacao > 3)
			variacao = 0;
	}

	private void detectarColisao() {

		//Cria os colisores do passaro e dos canos
		circuloPassaro.set(50 + passaros[0].getWidth()/2, posicaoInicialVertical + passaros[0].getHeight()/2, passaros[0].getHeight()/2);
		retanguloCanobaixo.set(posicaoCanoHorizontal, alturaDispositivo/2-canoBaixo.getHeight()-espacoCano/2+posicaoCanoVertical,canoBaixo.getWidth(), canoBaixo.getHeight());
		retanguloCanocima.set(posicaoCanoHorizontal, alturaDispositivo/2-canoBaixo.getHeight()-espacoCano/2+posicaoCanoVertical, canoBaixo.getWidth(),canoBaixo.getHeight());
		//boolean para checar a colisão
		boolean bateuCanoCima = Intersector.overlaps(circuloPassaro,retanguloCanocima);
		boolean bateuCanoBaixo = Intersector.overlaps(circuloPassaro,retanguloCanobaixo);

		//checa se colidiu
		if(bateuCanoBaixo || bateuCanoCima){
			if(estadoJogo == 1){
			    somColisao.play();
			    estadoJogo = 2;
            }
		}
	}

	private void verificaEstadojogo() {

		//verifica o toque na tela
		boolean toqueTela = Gdx.input.justTouched();

		if(estadoJogo == 0){
			//se ficar clicando a gravidade cai
			if(Gdx.input.justTouched())
			{
				gravidade = -15;
				estadoJogo = 1;
				somVoando.play();
			}

		} else if (estadoJogo == 1){

			if(Gdx.input.justTouched())
			{
				gravidade = -15;
				somVoando.play();
			}

			posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime()*200;
			if (posicaoCanoHorizontal < - canoBaixo.getHeight()){
				posicaoCanoHorizontal = larguraDispositivo;
				posicaoCanoVertical = random.nextInt(400)-200;
				passouCano = false;

				//verifica se tocou na tela ou a posição vertical é maior que zero
				if (posicaoInicialVertical > 0 || toqueTela) {
					//atualiza a posição vertical
					posicaoInicialVertical = posicaoInicialVertical - gravidade;

					//soma +1 na gravidade
					gravidade++;
				}
			}
		} else if (estadoJogo == 2){

		    if(pontos > pontuacaoMaxima){
		        pontuacaoMaxima = pontos;
		        preferencias.putInteger("pontuacaoMaxima", pontuacaoMaxima);
            }

		    posicaoHorizontalPassaro -= Gdx.graphics.getDeltaTime() * 500;

		    if(toqueTela){
		        estadoJogo = 0;
		        pontos = 0;
		        gravidade = 0;
		        posicaoHorizontalPassaro = 0;
		        posicaoInicialVertical = alturaDispositivo / 2;
		        posicaoCanoHorizontal = larguraDispositivo;
            }
		}

	}

	private void desenharTexturas() {
		batch.begin();
		//Configura as texturas na tela do jogo
		batch.draw(fundo, 0,0,larguraDispositivo,alturaDispositivo);
		batch.draw(passaros[(int) variacao],50 + posicaoHorizontalPassaro,posicaoInicialVertical);
		batch.draw(canoBaixo, posicaoCanoHorizontal, alturaDispositivo/2- canoBaixo.getHeight()-espacoCano/2+ posicaoCanoVertical);
		batch.draw(canoCima, posicaoCanoHorizontal, alturaDispositivo/2 + espacoCano/2 + posicaoCanoVertical);
		textoPontuacao.draw(batch, String.valueOf(pontos), larguraDispositivo/2,alturaDispositivo-100);

		if(estadoJogo == 2){
			batch.draw(gameOver, larguraDispositivo / 2 - gameOver.getDepth() / 2, alturaDispositivo / 2);
			textoIniciar.draw(batch, "TOQUE NA TELA PARA REINICIAR!", larguraDispositivo /2 - 200, alturaDispositivo/2 - gameOver.getHeight() / 2);
			textoMelhorPontuacao.draw(batch, "SUA MELHOR PONTUAÇÃO É : " + pontuacaoMaxima + "PONTOS!", larguraDispositivo /2 - 300, alturaDispositivo/2 - gameOver.getHeight() / 2);
		}

		batch.end();
	}

	@Override
	public void dispose ()
	{

	}
}
