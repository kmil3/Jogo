package com.badlogic.drop;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class Drop extends ApplicationAdapter {

    private Texture bola_1_png;
    private Texture bola_2_png;
    private Texture bola_3_png;
    private Texture bola_4_png;
    private Texture lagarta_PNG;
    private Sound dropSound;
    private Music rainMusic;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Rectangle lagarta;
    private Array<Rectangle> raindrops;
    private long lastDropTime;
    private State state;
	private BitmapFont font;

    @Override
    public void create() {
		//set initial state
		state = State.RUN;

        // load the images for the droplet and the bucket, 64x64 pixels each
        bola_1_png = new Texture(Gdx.files.internal("bola1.png"));
        bola_2_png = new Texture(Gdx.files.internal("bola2.png"));
        bola_3_png = new Texture(Gdx.files.internal("bola3.png"));
        bola_4_png = new Texture(Gdx.files.internal("bola4.png"));
        lagarta_PNG = new Texture(Gdx.files.internal("lagarta.png"));


        //create the Camera and the SpriteBatch
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        batch = new SpriteBatch();

        //creating the bucket
        lagarta = new Rectangle();
        lagarta.x = 800 / 2 - 64 / 2;
        lagarta.y = 20;
        lagarta.width = 50;
        lagarta.height = 50;

        //creating the raindrops
        raindrops = new Array<Rectangle>();
        spawnRaindrop();

		font = new BitmapFont();
    }

    //create a rain drop
    private void spawnRaindrop() {
        Rectangle raindrop = new Rectangle();
        raindrop.x = MathUtils.random(0, 800 - 64);
        raindrop.y = 480;
        raindrop.width = 64;
        raindrop.height = 64;
        raindrops.add(raindrop);
        lastDropTime = TimeUtils.nanoTime();
    }

    @Override
    public void render() {

		if (Gdx.input.isKeyPressed(Input.Keys.P))
			pause();
		if (Gdx.input.isKeyPressed(Input.Keys.R))
			resume();

		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(lagarta_PNG, lagarta.x, lagarta.y);
		for (Rectangle raindrop : raindrops) {
			batch.draw(bola_1_png, raindrop.x, raindrop.y);
		}
		batch.end();

		switch (state) {
            case RUN:
            	//check mouse input
                if (Gdx.input.isTouched()) {
                    Vector3 touchPos = new Vector3();
                    touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
                    camera.unproject(touchPos);
                    lagarta.x = touchPos.x - 64 / 2;
                }
                //check keyboard input
                if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
                    lagarta.x -= 200 * Gdx.graphics.getDeltaTime();
                if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
                    lagarta.x += 200 * Gdx.graphics.getDeltaTime();
                //check screen limits
                if (lagarta.x < 0)
                    lagarta.x = 0;
                if (lagarta.x > 800 - 64)
                    lagarta.x = 800 - 64;
                //check time to create another raindrop
                if (TimeUtils.nanoTime() - lastDropTime > 1000000000)
                    spawnRaindrop();
                //move raindrops created
                for (Iterator<Rectangle> it = raindrops.iterator(); it.hasNext(); ) {
                    Rectangle raindrop = it.next();
                    raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
                    //check if it is beyond the screen
                    if (raindrop.y + 64 < 0)
                        it.remove();
                    //check collision between bucket and raindrops
                    if (raindrop.overlaps(lagarta)) {
                        dropSound.play();
                        it.remove();
                    }
                }
                break;
            case PAUSE:
            	batch.begin();
				font.draw(batch, "PAUSED", 380, 250);
                batch.end();
				break;
        }


    }

	@Override
    public void pause() {
        this.state = State.PAUSE;
    }

    @Override
    public void resume() {
        this.state = State.RUN;
    }

	@Override
    public void dispose() {
        bola_1_png.dispose();
        lagarta_PNG.dispose();
        dropSound.dispose();
        rainMusic.dispose();
        batch.dispose();
    }

    public enum State {
        PAUSE,
        RUN,
    }
}
