package com.badlogic.drop;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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

    private Texture meteoro_1;
    private Texture meteoro_2;
    private Texture meteoro_3;
    private Texture meteoro_4;
    private Texture lagarta_PNG;
    private Texture background;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Rectangle lagarta;
    private Array<Rectangle> raindrops;
    private long lastDropTime;
    private State state;
	private BitmapFont font;
    private static int pontos = 0;

    @Override
    public void create() {
		//set initial state
		state = State.RUN;

        // load the images for the droplet and the bucket, 64x64 pixels each
        meteoro_1 = new Texture(Gdx.files.internal("meteoro_1.png"));
        meteoro_2 = new Texture(Gdx.files.internal("meteoro_2.png"));
        meteoro_3 = new Texture(Gdx.files.internal("meteoro_3.png"));
        meteoro_4 = new Texture(Gdx.files.internal("meteoro_4.png"));
        lagarta_PNG = new Texture(Gdx.files.internal("lagarta.png"));


        //create the Camera and the SpriteBatch
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        batch = new SpriteBatch();

        //creating the bucket
        lagarta = new Rectangle();
        lagarta.x = 800 / 2 - 64 / 2;
        lagarta.y = -20;
        lagarta.width = 64;
        lagarta.height = 64;

        //creating the raindrops
        raindrops = new Array<Rectangle>();
            spawnRaindrop();
            spawnRaindrop();
            spawnRaindrop();
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
        //Gdx.gl.glClearColor(0.3f, 0.5f, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        background = new Texture("fundo.png");
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
        batch.draw(background,0,0);
		batch.draw(lagarta_PNG, lagarta.x, lagarta.y);
        //ELE DESENHA AS 4 IMAGENS VÁRIAS VEZES NA MESMA POSIÇÃO
        for(Rectangle r: raindrops) {
            int x = MathUtils.random(0, 4);
            if (x == 0) {
                batch.draw(meteoro_1, r.x, r.y);
            } else if (x == 1) {
                batch.draw(meteoro_2, r.x, r.y);
            }else if(x ==2 ){
                batch.draw(meteoro_3, r.x, r.y);
            }else{
                batch.draw(meteoro_4, r.x, r.y);
            }
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
                int i = 0;
                for (Iterator<Rectangle> it = raindrops.iterator(); it.hasNext(); ) {
                    Rectangle raindrop = it.next();
                    raindrop.y -= 100 * Gdx.graphics.getDeltaTime();
                    //check if it is beyond the screen
                    if (raindrop.y + 64 < -20){
                        raindrop.y = 400-64;
                        raindrop.x = MathUtils.random(0, 800 - 64);
                    }
                    //check collision between bucket and raindrops
                    if (raindrop.overlaps(lagarta)) {
                        if(i == 0 || i==1){
                            pontos ++;
                        }else {
                           pontos --;
                        }
                        it.remove();
                    }
                    i ++;
                }

                break;
            case PAUSE:
            	batch.begin();
				font.draw(batch, "PAUSED", 380, 250);
                batch.end();
				break;
        }
        batch.begin();
        font.draw(batch, String.valueOf(pontos), 20, 350);
        batch.end();

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
        meteoro_1.dispose();
        meteoro_2.dispose();
        meteoro_3.dispose();
        meteoro_4.dispose();
        lagarta_PNG.dispose();
        batch.dispose();
    }

    public enum State {
        PAUSE,
        RUN,
    }
}
