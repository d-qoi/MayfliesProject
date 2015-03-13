package server;

import java.util.ArrayList;
import java.util.LinkedList;

import server.VirtualMachine;

public class Server {
	private int maxCore;
	private int maxRam;
	private int maxVM;
	private int usedCore;
	private int usedRam;
	private int numberVM;
	private double load;
	private ArrayList<VirtualMachine> vms;
	
	
	public Server(int maxCore, int maxRam, int maxVM) {
		super();
		if(maxVM == 0) {
			maxVM = Integer.MAX_VALUE;
		}
		this.maxCore = maxCore;
		this.maxRam = maxRam;
		this.maxVM = maxVM;
		vms = new ArrayList<VirtualMachine>();
		this.setLoad(Integer.MAX_VALUE);
	}

	public boolean addVM(VirtualMachine vm){
		if(this.canAddVM(vm)){
			this.usedCore = vm.getCpu() + this.usedCore;
			this.usedRam = vm.getRam() + this.usedRam;
			this.numberVM++;
			this.vms.add(vm);
			return true;
		}
		return false;
	}
	public void addVMUnchecked(VirtualMachine vm){
		this.usedCore = vm.getCpu() + this.usedCore;
		this.usedRam = vm.getRam() + this.usedRam;
		this.numberVM++;
		this.vms.add(vm);
	}
	public boolean canAddVM(VirtualMachine vm) {
		return (vm.getRam() + this.usedRam <= this.maxRam);
	}
	public boolean canAddVMIncludingCPU(VirtualMachine vm) {
		return (canAddVM(vm) && (vm.getCpu() + this.usedCore <= this.maxCore));
	}
	
	public LinkedList<VirtualMachine> tick(int tick) {
		LinkedList<VirtualMachine> reboundMayflies = new LinkedList<VirtualMachine>();
		for(int i = 0; i<this.vms.size();) {
			Object[] temp = this.vms.get(i).tick(tick);
			boolean completion = (boolean)temp[0];
			if(temp[1] != null) {
				reboundMayflies.add((VirtualMachine)temp[1]);
			}
			if(completion) {
				i++;
			}
			else {
				this.usedCore = this.usedCore - this.vms.get(i).getCpu();
				this.usedRam = this.usedRam - vms.get(i).getRam();
				this.numberVM--;
				if(vms.get(i).isMayfly()) {
					
				}
				this.vms.remove(i);
			}
		}
		return reboundMayflies;
	}
	public void update() {
		this.usedCore = 0;
		this.usedRam = 0;
		for(VirtualMachine vm : vms) {
			vm.update();
			this.usedCore = this.usedCore + vm.getCpu();
			this.usedRam = this.usedRam + vm.getRam();
		}
		updateLoad();
	}
	
	public void updateLoad() {
		this.load = (this.usedRam!=0?((double)this.maxRam)/((double)this.usedRam):this.maxRam);
	}
	public double getRamPercentUtilization() {
		return ((double)this.usedRam/(double)this.maxRam)*100.0;
	}
	public double getCPUPercentUtilization() {
		return ((double)this.usedCore/(double)this.maxCore)*100.0;
	}
	public int getMaxCore() {
		return maxCore;
	}
	public void setMaxCore(int maxCore) {
		this.maxCore = maxCore;
	}
	public int getMaxRam() {
		return maxRam;
	}
	public void setMaxRam(int maxRam) {
		this.maxRam = maxRam;
	}
	public int getMaxVM() {
		return maxVM;
	}
	public void setMaxVM(int maxVM) {
		this.maxVM = maxVM;
	}
	public int getUsedCore() {
		return usedCore;
	}
	public void setUsedCore(int usedCore) {
		this.usedCore = usedCore;
	}
	public int getUsedRam() {
		return usedRam;
	}
	public void setUsedRam(int usedRam) {
		this.usedRam = usedRam;
	}
	public int getNumberVM() {
		return numberVM;
	}
	
	public double getLoad() {
		return load;
	}

	public void setLoad(double load) {
		this.load = load;
	}
	
	public Server basicClone() {
		return new Server(maxCore, maxRam, maxVM);
	}

	public String scrape() {
		return String.format("Server [maxCore=%d, usedCore=%d, maxRam=%d, usedRam=%d,VMS=%d, load=%.3f",
				maxCore, usedCore, maxRam, usedRam, numberVM, load);
	}

	@Override
	public String toString() {
		return "Server [maxCore=" + maxCore + ", maxRam=" + maxRam + ", maxVM="
				+ maxVM + ", usedCore=" + usedCore + ", usedRam=" + usedRam
				+ ", numberVM=" + numberVM + ", load="+String.format("%.3f", load)+", vms=" + vms + "]";
	}
}
