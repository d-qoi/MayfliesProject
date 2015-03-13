package server;



public class VirtualMachine {
	private int life;
	private int maxLife;
	private int cpu;
	private int cpu_range;
	private int ram;
	//private double tickLifeIncrease;
	private int tickStart;
	private int tickEnd;
	private boolean mayfly;
	private boolean spiked = false;
	private int spikeChance;
	private int spike; //noise
	private boolean persistent;
	private int mayflyLife;

	public VirtualMachine(int cpu, int cpu_range, int ram, int spike, int mayflyLife) {
		super();
		this.persistent = false;
		this.cpu = cpu;
		this.cpu_range = cpu_range;
		this.ram = ram;
		this.spike = spike;
		this.life = 0;
		this.mayflyLife = mayflyLife;
	}
	

	public VirtualMachine(int life, int maxLife, int cpu, int cpu_range,
			int ram, int tickStart, int tickEnd, boolean mayfly,
			boolean spiked, int spikeChance, int spike,
			boolean persistent, int mayflyLife) {
		super();
		this.life = life;
		this.maxLife = maxLife;
		this.cpu = cpu;
		this.cpu_range = cpu_range;
		this.ram = ram;
		this.tickStart = tickStart;
		this.tickEnd = tickEnd;
		this.mayfly = mayfly;
		this.spiked = spiked;
		this.spikeChance = spikeChance;
		this.spike = spike;
		this.persistent = persistent;
		this.mayflyLife = mayflyLife;
	}


	public Object[] tick(int tick) {
		Object[] out = new Object[2];
		out[0] = false;
		out[1] = null;
		this.life++;
		this.spike();
		if(this.mayfly && !this.persistent && this.mayflyLife > 0 && this.life > 0 && this.life%this.mayflyLife == 0) {
			out[1] = this.clone();
			return out;
		}	
		if(this.maxLife > 0) {
			out[0] = (this.life < this.maxLife);
			return out;
		}
		
		out[0] = (tick < this.tickEnd);
		return out;
	}
	
	public void update() {
		
	}
	
	private void spike() {
		if(this.spiked) {
			this.cpu = this.cpu - this.spike;
		}
		else {
			if((int)(Math.random()*100) < this.spikeChance) {
				this.cpu = this.cpu + this.spike;
				//System.out.println("Spiked");
			}			
		}		
	}
	
	public int getLife() {
		return life;
	}

	public void setLife(int life) {
		if(life == 0)
			return;
		this.life = life;
	}

	public int getMaxLife() {
		return maxLife;
	}

	public void setMaxLife(int maxLife) {
		this.maxLife = maxLife;
	}

	public int getCpu() {
		return cpu;
	}

	public void setCpu(int cpu) {
		if(cpu == 0)
			return;
		this.cpu = cpu;
	}

	public int getCpu_range() {
		return cpu_range;
	}

	public void setCpu_range(int cpu_range) {
		this.cpu_range = cpu_range;
	}

	public int getRam() {
		return ram;
	}

	public void setRam(int ram) {
		if(ram == 0)
			return;
		this.ram = ram;
	}

	public int getTickStart() {
		return tickStart;
	}

	public void setTickStart(int tickStart) {
		this.tickStart = tickStart;
	}

	public int getTickEnd() {
		return tickEnd;
	}

	public void setTickEnd(int tickEnd) {
		this.tickEnd = tickEnd;
	}

	public boolean isMayfly() {
		return mayfly;
	}

	public void setMayfly(boolean mayfly) {
		this.mayfly = mayfly;
	}

	public int getSpikeChance() {
		return spikeChance;
	}

	public void setSpikeChance(int spikeChance) {
		this.spikeChance = spikeChance;
	}

	public int getSpike() {
		return spike;
	}

	public void setSpike(int spike) {
		this.spike = spike;
	}

	public boolean isSpiked() {
		return spiked;
	}
	
	public boolean isPersistent() {
		return persistent;
	}

	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}

	public int getMayflyLife() {
		return mayflyLife;
	}

	public void setMayflyLife(int mayflyLife) {
		this.mayflyLife = mayflyLife;
	}

	public  VirtualMachine clone() {
		return new VirtualMachine(life, maxLife, cpu, cpu_range, ram, tickStart, tickEnd, mayfly, spiked, spikeChance, spike, persistent, mayflyLife);
	}

	@Override
	public String toString() {
		return "VirtualMachine [Persistent=" + persistent + ", life=" + life + ", maxLife=" + maxLife
				+ ", cpu=" + cpu + ", cpu_range=" + cpu_range + ", ram=" + ram
				+ ", tickStart=" + tickStart + ", tickEnd=" + tickEnd
				+ ", mayfly=" + mayfly + ", spiked="
				+ spiked + ", spikeChance=" + spikeChance + ", spike=" + spike
				+ "]";
	}
	

	
	
	
}
